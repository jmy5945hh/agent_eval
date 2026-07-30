package com.example.agenteval.domain.service;

import java.io.OutputStream;

/**
 * 导出服务接口 — 负责评分明细的 Excel 生成。
 *
 * <h4>导出内容</h4>
 * <p>生成评分维度 × 案例的二维表格，每行包含：
 * 案例名称、各维度得分、加权总分、评语、是否人工修改。</p>
 *
 * <h4>技术方案</h4>
 * <p>使用 Apache POI 生成 .xlsx 文件，通过 OutputStream 写入响应体。</p>
 */
public interface ExportService {

    /**
     * 导出任务的评分明细为 Excel。
     * <p>文件名格式：scores-{taskName}-{taskId}.xlsx</p>
     *
     * @param taskId 任务 ID
     * @param outputStream 输出流（通常为 HttpServletResponse.getOutputStream()）
     * @return 建议的文件名
     */
    String exportScores(Long taskId, OutputStream outputStream);
}
