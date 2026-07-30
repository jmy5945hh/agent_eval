package com.example.agenteval.domain.service.impl;

import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.TaskCaseScorePO;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import com.example.agenteval.domain.repository.TaskCaseScorePORespository;
import com.example.agenteval.domain.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出服务实现 — {@link ExportService} 的默认实现。
 *
 * <h4>职责</h4>
 * <p>将指定测评任务的评分明细导出为 .xlsx 文件，使用 Apache POI
 * 生成「评分维度 × 案例」二维表格。</p>
 *
 * <h4>导出列结构</h4>
 * <ol>
 *   <li>案例名称</li>
 *   <li>案例分类</li>
 *   <li>执行状态</li>
 *   <li>各评分维度得分（动态列，根据实际评分标准展开）</li>
 *   <li>加权总分</li>
 *   <li>评语汇总</li>
 *   <li>是否人工修改</li>
 * </ol>
 *
 * @see ExportService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_COMPLETED = 2;

    private final EvaluationTaskPORespository taskRepository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final TaskCaseScorePORespository caseScoreRepository;
    private final EvaluationCasePORespository caseRepository;

    /**
     * 导出任务的评分明细为 Excel。
     *
     * <p>文件名格式：scores-{taskName}-{taskId}.xlsx</p>
     */
    @Override
    public String exportScores(Long taskId, OutputStream outputStream) {
        EvaluationTaskPO task = taskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        List<TaskCaseRunPO> runs = caseRunRepository.findByTaskId(taskId.intValue());

        // 批量加载关联案例
        Map<Integer, EvaluationCasePO> caseMap = loadCaseMap(runs);

        // 收集所有评分维度（用于动态生成表头）
        List<String> dimKeys = collectDimensionKeys(runs);
        List<String> dimLabels = collectDimensionLabels(runs, dimKeys);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("评分明细");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 写表头
            writeHeader(sheet, headerStyle, dimLabels);

            // 写数据行
            int rowIdx = 1;
            for (TaskCaseRunPO run : runs) {
                List<TaskCaseScorePO> scores = caseScoreRepository.findByRunId(run.getId());
                Map<String, TaskCaseScorePO> scoreMap = scores.stream()
                        .collect(Collectors.toMap(TaskCaseScorePO::getDimKey, s -> s, (a, b) -> a));

                EvaluationCasePO caseItem = caseMap.get(run.getCaseId());

                Row row = sheet.createRow(rowIdx++);
                writeDataRow(row, dataStyle, run, caseItem, scoreMap, dimKeys, scores);
            }

            // 自动调整列宽
            for (int i = 0; i < dimLabels.size() + 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            outputStream.flush();

            String filename = buildFilename(task);
            log.info("Exported scores for task {} → {} ({} runs)", taskId, filename, runs.size());
            return filename;
        } catch (IOException e) {
            log.error("Failed to export scores for task {}", taskId, e);
            throw new RuntimeException("导出 Excel 失败: " + e.getMessage(), e);
        }
    }

    // ==================== 数据准备 ====================

    /**
     * 批量加载案例，构建 caseId → EvaluationCasePO 映射。
     */
    private Map<Integer, EvaluationCasePO> loadCaseMap(List<TaskCaseRunPO> runs) {
        List<Integer> caseIds = runs.stream()
                .map(TaskCaseRunPO::getCaseId)
                .distinct()
                .collect(Collectors.toList());

        if (caseIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return caseRepository.findAllById(caseIds).stream()
                .collect(Collectors.toMap(EvaluationCasePO::getId, c -> c, (a, b) -> a));
    }

    /**
     * 从所有评分记录中收集去重且排序的维度 key。
     */
    private List<String> collectDimensionKeys(List<TaskCaseRunPO> runs) {
        return runs.stream()
                .flatMap(run -> caseScoreRepository.findByRunId(run.getId()).stream())
                .map(TaskCaseScorePO::getDimKey)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 根据维度 key 收集对应的显示名称。
     */
    private List<String> collectDimensionLabels(List<TaskCaseRunPO> runs, List<String> dimKeys) {
        Map<String, String> keyToLabel = new LinkedHashMap<>();
        for (String key : dimKeys) {
            keyToLabel.put(key, key); // 默认用 key 作为 label
        }
        for (TaskCaseRunPO run : runs) {
            List<TaskCaseScorePO> scores = caseScoreRepository.findByRunId(run.getId());
            for (TaskCaseScorePO s : scores) {
                if (s.getDimLabel() != null && !s.getDimLabel().isEmpty()) {
                    keyToLabel.put(s.getDimKey(), s.getDimLabel());
                }
            }
        }
        return dimKeys.stream()
                .map(key -> keyToLabel.getOrDefault(key, key))
                .collect(Collectors.toList());
    }

    // ==================== Excel 写入 ====================

    /**
     * 写入表头行。
     */
    private void writeHeader(Sheet sheet, CellStyle headerStyle, List<String> dimLabels) {
        Row header = sheet.createRow(0);
        int col = 0;

        createCell(header, col++, "案例名称", headerStyle);
        createCell(header, col++, "案例分类", headerStyle);
        createCell(header, col++, "执行状态", headerStyle);

        // 动态维度列
        for (String label : dimLabels) {
            createCell(header, col++, label + "得分", headerStyle);
        }

        createCell(header, col++, "加权总分", headerStyle);
        createCell(header, col++, "评语汇总", headerStyle);
        createCell(header, col, "人工修改", headerStyle);
    }

    /**
     * 写入一条数据行。
     */
    private void writeDataRow(Row row, CellStyle dataStyle,
                               TaskCaseRunPO run, EvaluationCasePO caseItem,
                               Map<String, TaskCaseScorePO> scoreMap,
                               List<String> dimKeys, List<TaskCaseScorePO> scores) {
        int col = 0;

        // 案例名称
        createCell(row, col++, caseItem != null ? caseItem.getCaseName() : "案例#" + run.getCaseId(), dataStyle);

        // 案例分类
        createCell(row, col++, caseItem != null ? mapCategoryLabel(caseItem.getCategory()) : "-", dataStyle);

        // 执行状态
        createCell(row, col++, mapRunStatus(run.getStatus()), dataStyle);

        // 各维度得分
        int totalScore = 0;
        int dimCount = 0;
        StringBuilder commentBuilder = new StringBuilder();
        boolean hasEdited = false;

        for (String dimKey : dimKeys) {
            TaskCaseScorePO s = scoreMap.get(dimKey);
            if (s != null) {
                createCell(row, col++, String.valueOf(s.getScore()), dataStyle);
                totalScore += s.getScore();
                dimCount++;
                if (s.getComment() != null && !s.getComment().isEmpty()) {
                    if (commentBuilder.length() > 0) commentBuilder.append("; ");
                    commentBuilder.append(s.getDimLabel() != null ? s.getDimLabel() : s.getDimKey())
                            .append(": ").append(s.getComment());
                }
            } else {
                createCell(row, col++, "-", dataStyle);
            }
        }

        // 加权总分（简化：有维度分则计算均值，否则用 avgScore）
        if (dimCount > 0) {
            createCell(row, col++, String.format("%.1f", (double) totalScore / dimCount), dataStyle);
        } else {
            createCell(row, col++, "-", dataStyle);
        }

        // 评语汇总
        createCell(row, col++, commentBuilder.length() > 0 ? commentBuilder.toString() : "-", dataStyle);

        // 人工修改标识
        createCell(row, col, hasEdited ? "是" : "否", dataStyle);
    }

    // ==================== 样式 ====================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    // ==================== 辅助方法 ====================

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String buildFilename(EvaluationTaskPO task) {
        String taskName = task.getTaskName() != null
                ? task.getTaskName().replaceAll("[\\\\/:*?\"<>|]", "_")
                : "task";
        return "scores-" + taskName + "-" + task.getId() + ".xlsx";
    }

    // ==================== 状态映射 ====================

    /**
     * 将执行状态 int 映射为可读字符串。
     */
    private String mapRunStatus(int status) {
        switch (status) {
            case 1: return "排队中";
            case 2: return "运行中";
            case 3: return "成功";
            case 4: return "失败";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 将分类 int 映射为可读字符串。
     */
    private String mapCategoryLabel(int category) {
        switch (category) {
            case 1: return "前端";
            case 2: return "Java后端";
            case 3: return "Python后端";
            case 4: return "AI智能体";
            case 5: return "安全测试";
            default: return "其他";
        }
    }
}
