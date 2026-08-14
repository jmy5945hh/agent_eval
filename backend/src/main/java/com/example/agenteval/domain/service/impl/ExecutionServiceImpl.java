package com.example.agenteval.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.application.dto.request.record.TaskCaseListRequest;
import com.example.agenteval.application.dto.response.record.*;
import com.example.agenteval.domain.model.*;
import com.example.agenteval.domain.model.excel.RecordListExcel;
import com.example.agenteval.domain.model.pojo.CaseRun;
import com.example.agenteval.domain.model.pojo.ErrorInfo;
import com.example.agenteval.domain.model.pojo.RunScore;
import com.example.agenteval.domain.model.pojo.ScoreCommentResult;
import com.example.agenteval.domain.repository.*;
import com.example.agenteval.domain.service.ExecutionService;
import com.example.agenteval.domain.service.specification.EvaluationTaskPOSpecs;
import com.example.agenteval.domain.service.specification.TaskCaseRunPOSpecs;
import com.example.agenteval.infrastructure.constant.ExcelConstant;
import com.example.agenteval.infrastructure.enums.CaseRunStatusEnum;
import com.example.agenteval.infrastructure.enums.ScoringStatusEnum;
import com.example.agenteval.infrastructure.enums.TaskStatusEnum;
import com.example.agenteval.infrastructure.util.DateUtil;
import com.example.agenteval.infrastructure.util.EnumUtil;
import com.example.agenteval.infrastructure.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 测评记录查询服务实现 — {@link ExecutionService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>历史记录分页查询：支持 Agent、Model、Status、时间范围多维度筛选。</li>
 *   <li>记录详情聚合：将任务信息、执行统计、评分汇总组装为完整视图。</li>
 * </ul>
 *
 * @see ExecutionService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExecutionServiceImpl implements ExecutionService {

    private static final Integer SCALE_FORE = 4;
    private static final String PERCENT = "%";
    private final EvaluationTaskPORespository taskRepository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final TaskCaseScorePORespository caseScoreRepository;
    private final AgentInfoPORespository agentInfoPORespository;
    private final ModelConfigPORespository modelConfigPORespository;
    private final EvaluationTaskPORespository evaluationTaskPORespository;
    private final AgentVersionPORespository agentVersionPORespository;
    private final EvaluationCasePORespository evaluationCasePORespository;
    private final ScoringStandardPORespository scoringStandardPORespository;
    private final TaskCaseRunPORespository taskCaseRunPORespository;
    private final MinioService minioService;
    private final TaskCaseScorePORespository taskCaseScorePORespository;
    @Value("${temp-file_path}")
    private String tempFilePath;

    @Override
    public SummaryDataResponse summaryData() {
        //全部
        long count = taskRepository.count();
        //完成任务
        int completionCount = taskRepository.countByStatus(TaskStatusEnum.RUNNING.getStatus());
        //完成率
        BigDecimal countDecimal = BigDecimal.valueOf(completionCount);
        BigDecimal completionDecimal = BigDecimal.valueOf(completionCount);
        String completionRate = completionDecimal.divide(countDecimal, SCALE_FORE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString().concat(PERCENT);
        //执行中
        List<EvaluationTaskPO> byStatus = taskRepository.findByStatus(TaskStatusEnum.RUNNING.getStatus());
        //队列中的案例
        List<Integer> taskIds = byStatus.stream().map(EvaluationTaskPO::getId).collect(Collectors.toList());
        List<TaskCaseRunPO> taskCaseRunPOS = caseRunRepository.findByTaskIdIn(taskIds);
        long queueCase = taskCaseRunPOS.stream().filter(item -> CaseRunStatusEnum.QUEUED.getStatus().equals(item.getStatus())).count();
        //近30天平均得分
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Double avg = taskRepository.findAvgScoreOfCompletedTasksInLast30Days(TaskStatusEnum.COMPLETED.getStatus(), thirtyDaysAgo);
        return SummaryDataResponse.builder().taskCount((int) count).completionCount(completionCount).runCount(CollUtil.isEmpty(byStatus) ? 0 : byStatus.size())
                .queueCases((int) queueCase).averageScore(ObjUtil.isNull(avg) ? 0 : avg.intValue())
                .completionRate(completionRate).build();
    }

    @Override
    public Page<RecordListResponse> recordList(RecordListRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<EvaluationTaskPO> evaluationTaskList = taskRepository.findAll(EvaluationTaskPOSpecs.recordListBuildSpec(request), pageable);
        List<RecordListResponse> returnList = getRecordListResponse(evaluationTaskList);
        return new PageImpl<>(returnList, evaluationTaskList.getPageable(), evaluationTaskList.getTotalElements());
    }

    @Override
    public void exportRecord(HttpServletResponse response, RecordListRequest request) {
        List<EvaluationTaskPO> allRecord = taskRepository.findAll(EvaluationTaskPOSpecs.recordListBuildSpec(request));
        List<RecordListResponse> returnList = getRecordListResponse(allRecord);
        List<RecordListExcel> excelList = new ArrayList<>(returnList.size());
        returnList.forEach(item -> excelList.add(RecordListExcel.builder().taskName(item.getTaskName()).agentName(item.getAgentName())
                .modelName(item.getModelName()).caseCount(item.getCaseCount())
                .taskStatus(EnumUtil.findEnumByField(TaskStatusEnum.class, TaskStatusEnum.STATUS_CONSTANT, item.getTaskStatus()).getInterpretation())
                .scoreStatus(EnumUtil.findEnumByField(ScoringStatusEnum.class, ScoringStatusEnum.STATUS_CONSTANT, item.getScoreStatus()).getInterpretation())
                .taskCreateUserName(item.getTaskCreateUserName()).taskCreateTaskTime(item.getTaskCreateTaskTime()).build()));
        String fileName = IdUtil.simpleUUID() + ExcelConstant.SUFFIX;
        String filePath = tempFilePath + fileName;
        ExcelUtil.writeExcel(filePath, ExcelConstant.RECORD_EXCEL_SHEET_NAME, excelList, RecordListExcel.class);
        try (OutputStream out = response.getOutputStream();
             InputStream inputStream = new FileInputStream(filePath)) {
            byte[] fileByte = IOUtils.toByteArray(inputStream);
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.addHeader("Content-Length", String.valueOf(fileByte.length));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            out.write(fileByte);
            out.flush();
        } catch (Exception e) {
            log.error("下载评测文件失败，失败原因:[{}]", e.getMessage(), e);
        } finally {
            new File(filePath).delete();
        }
    }

    @Override
    public TaskDetailResponse taskDetail(Integer taskId) {
        EvaluationTaskPO evaluationTaskPO = evaluationTaskPORespository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        //agent
        AgentInfoPO agentInfoPO = agentInfoPORespository.findById(evaluationTaskPO.getAgentId()).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + evaluationTaskPO.getAgentId()));
        //agent version
        AgentVersionPO agentVersionPO = agentVersionPORespository.findById(evaluationTaskPO.getAgentVersionId()).orElseThrow(() -> new IllegalArgumentException("Agent 版本不存在: " + evaluationTaskPO.getAgentVersionId()));
        //model
        ModelConfigPO modelConfigPO = modelConfigPORespository.findById(evaluationTaskPO.getModelId()).orElseThrow(() -> new IllegalArgumentException("模型不存在: " + evaluationTaskPO.getModelId()));
        //scoring standard
        ScoringStandardPO scoringStandardPO = scoringStandardPORespository.findById(evaluationTaskPO.getScoreStandardId()).orElseThrow(() -> new IllegalArgumentException("评测标准不存在: " + evaluationTaskPO.getScoreStandardId()));
        //案例
        List<TaskCaseRunPO> taskCaseRunPOS = taskCaseRunPORespository.findByTaskId(evaluationTaskPO.getId());
        Map<Integer, List<TaskCaseRunPO>> taskCaseRunMap = taskCaseRunPOS.stream().collect(Collectors.groupingBy(TaskCaseRunPO::getStatus));

        TaskDetailResponse.ExecutionProgress executionProgress = TaskDetailResponse.ExecutionProgress.builder().totalCase(taskCaseRunPOS.size())
                .successCase(ObjUtil.isNull(taskCaseRunMap.get(CaseRunStatusEnum.SUCCESS.getStatus())) ? 0 : taskCaseRunMap.get(CaseRunStatusEnum.SUCCESS.getStatus()).size())
                .failCase(ObjUtil.isNull(taskCaseRunMap.get(CaseRunStatusEnum.FAILED.getStatus())) ? 0 : taskCaseRunMap.get(CaseRunStatusEnum.FAILED.getStatus()).size())
                .failCase(ObjUtil.isNull(taskCaseRunMap.get(CaseRunStatusEnum.QUEUED.getStatus())) ? 0 : taskCaseRunMap.get(CaseRunStatusEnum.QUEUED.getStatus()).size())
                .build();
        return TaskDetailResponse.builder().id(evaluationTaskPO.getId()).taskName(evaluationTaskPO.getTaskName()).agentName(agentInfoPO.getAgentName()).agentVersion(agentVersionPO.getVersion())
                .modelName(modelConfigPO.getModelName()).scoringStandardName(scoringStandardPO.getScoringStandardName()).executionProgress(executionProgress).build();
    }

    @Override
    public Page<TaskCaseListResponse> taskCaseList(Integer taskId, TaskCaseListRequest taskCaseListRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(taskCaseListRequest.getPage(), taskCaseListRequest.getSize(), sort);
        Page<TaskCaseRunPO> taskCaseRunPage = taskCaseRunPORespository.findAll(TaskCaseRunPOSpecs.taskCaseListBuildSpec(taskId, taskCaseListRequest.getState()), pageable);
        List<TaskCaseRunPO> taskCaseRunPOS = taskCaseRunPage.getContent();
        Map<Integer, TaskCaseRunPO> taskCaseRunPOMap = taskCaseRunPOS.stream().collect(Collectors.toMap(TaskCaseRunPO::getId, Function.identity()));
        List<Integer> caseIds = taskCaseRunPOS.stream().map(TaskCaseRunPO::getCaseId).collect(Collectors.toList());
        //案例
        List<EvaluationCasePO> casePOList = evaluationCasePORespository.findByIdIn(caseIds);
        Map<Integer, EvaluationCasePO> evaluationCasePOMap = casePOList.stream().collect(Collectors.toMap(EvaluationCasePO::getId, Function.identity()));
        //返回体
        List<TaskCaseListResponse> taskCaseListResponses = new ArrayList<>(taskCaseRunPOS.size());

        taskCaseRunPOS.forEach(item -> {
            TaskCaseRunPO taskCaseRunPO = taskCaseRunPOMap.get(item.getId());
            EvaluationCasePO evaluationCasePO = evaluationCasePOMap.get(item.getCaseId());
            taskCaseListResponses.add(TaskCaseListResponse.builder().id(taskCaseRunPO.getId()).turn(item.getRounds() + "轮").token(item.getTokensOut() + item.getTokensIn())
                    .timeConsuming(DateUtil.calculateTimeDifference(item.getDurationMs())).score(item.getScore().longValue() + "分")
                    .state(taskCaseRunPO.getStatus()).caseName(evaluationCasePO.getCaseName()).build());
        });
        return new PageImpl<>(taskCaseListResponses, taskCaseRunPage.getPageable(), taskCaseRunPage.getTotalElements());
    }

    @Override
    public TaskCaseInfoResponse taskCaseInfo(Integer runCaseId) {
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findById(runCaseId).orElseThrow(() -> new IllegalArgumentException("任务案例不存在: " + runCaseId));
        return TaskCaseInfoResponse.builder().turn(taskCaseRunPO.getRounds() + "轮").state(taskCaseRunPO.getStatus()).inputToken(taskCaseRunPO.getTokensIn())
                .outputToken(taskCaseRunPO.getTokensOut()).build();
    }

    @Override
    public String taskCaseExecutionTrace(Integer runCaseId) {
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findById(runCaseId).orElseThrow(() -> new IllegalArgumentException("任务案例不存在: " + runCaseId));
        if (!CaseRunStatusEnum.SUCCESS.getStatus().equals(taskCaseRunPO.getStatus())) {
            return "";
        }
        try {
            return minioService.getAndReadFile(taskCaseRunPO.getSessionId() + ".jsonl");
        } catch (Exception e) {
            log.error("查询任务案例执行轨迹失败，失败原因:[{}]", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String taskCasePrompt(Integer runCaseId) {
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findById(runCaseId).orElseThrow(() -> new IllegalArgumentException("任务案例不存在: " + runCaseId));
        EvaluationCasePO evaluationCasePO = evaluationCasePORespository.findById(taskCaseRunPO.getCaseId()).orElseThrow(() -> new IllegalArgumentException("案例不存在: " + runCaseId));
        try {
            return minioService.getAndReadFile(evaluationCasePO.getPromptKey());
        } catch (Exception e) {
            log.error("查询任务案例提示词失败，失败原因:[{}]", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String taskCaseError(Integer runCaseId) {
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findById(runCaseId).orElseThrow(() -> new IllegalArgumentException("任务案例不存在: " + runCaseId));
        if (!CaseRunStatusEnum.FAILED.getStatus().equals(taskCaseRunPO.getStatus())) {
            return "";
        }
        if (StrUtil.isBlank(taskCaseRunPO.getErrorInfoKey())) {
            return "";
        }
        try {
            return minioService.getAndReadFile(taskCaseRunPO.getErrorInfoKey());
        } catch (Exception e) {
            log.error("查询任务案例执行轨迹失败，失败原因:[{}]", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public List<ScoreCommentResult> taskCaseScoreComment(Integer runCaseId) {
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findById(runCaseId).orElseThrow(() -> new IllegalArgumentException("任务案例不存在: " + runCaseId));
        if (!CaseRunStatusEnum.SUCCESS.getStatus().equals(taskCaseRunPO.getEvalStatus())) {
            return new ArrayList<>();
        }
        List<TaskCaseScorePO> taskCaseScorePOS = taskCaseScorePORespository.findByRunId(taskCaseRunPO.getId());
        List<ScoreCommentResult> returnList = new ArrayList<>(taskCaseScorePOS.size());
        taskCaseScorePOS.forEach(item -> {
            returnList.add(ScoreCommentResult.builder().label(item.getDimLabel()).key(item.getDimKey()).score(item.getScore()).comment(item.getComment()).build());
        });
        return returnList;
    }


    private List<RecordListResponse> getRecordListResponse(Object obj) {
        List<EvaluationTaskPO> evaluationTaskList = null;
        if (obj instanceof Page) {
            Page<?> page = (Page<?>) obj;
            evaluationTaskList = (List<EvaluationTaskPO>) page.getContent();
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            evaluationTaskList = (List<EvaluationTaskPO>) list;
        }
        //agent
        List<Integer> agentIds = evaluationTaskList.stream().map(EvaluationTaskPO::getAgentId).collect(Collectors.toList());
        List<AgentInfoPO> agentInfoList = agentInfoPORespository.findByIdIn(agentIds);
        Map<Integer, AgentInfoPO> agentInfoMap = agentInfoList.stream().collect(Collectors.toMap(AgentInfoPO::getId, Function.identity()));
        //model
        List<Integer> modelIds = evaluationTaskList.stream().map(EvaluationTaskPO::getModelId).collect(Collectors.toList());
        List<ModelConfigPO> modelConfigList = modelConfigPORespository.findByIdIn(modelIds);
        Map<Integer, ModelConfigPO> modelMap = modelConfigList.stream().collect(Collectors.toMap(ModelConfigPO::getId, Function.identity()));
        //案例个数
        List<Integer> taskId = evaluationTaskList.stream().map(EvaluationTaskPO::getId).collect(Collectors.toList());
        List<TaskCaseRunPO> caseRunList = caseRunRepository.findByTaskIdIn(taskId);
        Map<Integer, List<TaskCaseRunPO>> caseRunGroupMap = caseRunList.stream().collect(Collectors.groupingBy(TaskCaseRunPO::getTaskId));
        List<RecordListResponse> returnList = new ArrayList<>();
        evaluationTaskList.forEach(item -> {
            AgentInfoPO agentInfoPO = agentInfoMap.get(item.getAgentId());
            ModelConfigPO modelConfigPO = modelMap.get(item.getModelId());
            List<TaskCaseRunPO> taskCaseRunPOS = caseRunGroupMap.get(item.getId());
            returnList.add(RecordListResponse.builder().id(item.getId()).taskName(item.getTaskName()).agentName(agentInfoPO.getAgentName()).modelName(modelConfigPO.getModelName())
                    .caseCount(taskCaseRunPOS.size()).taskStatus(item.getStatus()).scoreStatus(item.getScoringStatus()).taskCreateUserName(item.getCreateUserName())
                    .taskCreateTaskTime(LocalDateTimeUtil.formatNormal(item.getCreateTime())).build());
        });
        return returnList;
    }

    // ==================== 映射转换 ====================

    /**
     * 将 {@link TaskCaseRunPO} 转换为 {@link CaseRun}。
     * <p>同时查询关联的评分维度数据组装 {@link RunScore}。</p>
     */
    private CaseRun toCaseRun(TaskCaseRunPO entity) {
        // 查询评分维度
        List<TaskCaseScorePO> scores = caseScoreRepository.findByRunId(entity.getId());

        RunScore runScore = null;
        if (!scores.isEmpty()) {
            Map<String, Integer> dims = new LinkedHashMap<>();
            Map<String, String> comments = new LinkedHashMap<>();
            for (TaskCaseScorePO s : scores) {
                dims.put(s.getDimKey(), s.getScore());
                if (s.getComment() != null) {
                    comments.put(s.getDimKey(), s.getComment());
                }
            }
            runScore = RunScore.builder()
                    .dims(dims)
                    .comments(comments)
                    .build();
        }

        // 错误信息（简化处理：仅在有 errorInfoKey 时构造）
        ErrorInfo error = null;
        if (entity.getErrorInfoKey() != null && !entity.getErrorInfoKey().isEmpty()) {
            error = new ErrorInfo("agent", entity.getErrorInfoKey());
        }

        return CaseRun.builder()
                .caseId(String.valueOf(entity.getCaseId()))
                .status(mapRunStatus(entity.getStatus()))
                .attempts(entity.getAttempts())
                .rounds(entity.getRounds())
                .tokensIn(entity.getTokensIn())
                .tokensOut(entity.getTokensOut())
                .durationMs(entity.getDurationMs())
                .error(error)
                .score(runScore)
                .removed(false)
                .build();
    }

    // ==================== 状态映射 ====================

    /**
     * 将请求中的状态字符串映射为数据库 int 值。
     * <ul>
     *   <li>running → 1</li>
     *   <li>completed → 2</li>
     *   <li>cancelled → 3</li>
     * </ul>
     */
    private int mapTaskStatus(String status) {
        if (status == null) return 1;
        switch (status.toLowerCase()) {
            case "running":
                return 1;
            case "completed":
                return 2;
            case "cancelled":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * 将执行记录状态 int 映射为字符串。
     * <ul>
     *   <li>1 → "queued"</li>
     *   <li>2 → "running"</li>
     *   <li>3 → "success"</li>
     *   <li>4 → "failed"</li>
     *   <li>5 → "cancelled"</li>
     * </ul>
     */
    private String mapRunStatus(int status) {
        switch (status) {
            case 1:
                return "queued";
            case 2:
                return "running";
            case 3:
                return "success";
            case 4:
                return "failed";
            case 5:
                return "cancelled";
            default:
                return "queued";
        }
    }
}
