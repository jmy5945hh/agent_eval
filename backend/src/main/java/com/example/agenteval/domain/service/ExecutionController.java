package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.application.dto.response.record.RecordListResponse;
import com.example.agenteval.application.dto.response.record.SummaryDataResponse;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;

/**
 * 测评记录查询服务接口 — 负责历史记录的分页、筛选、详情聚合查询。
 *
 * <h4>筛选维度</h4>
 * <ul>
 *   <li>Agent ID：按参测 Agent 过滤。</li>
 *   <li>Model ID：按测评模型过滤。</li>
 *   <li>Task Status：running / completed / cancelled。</li>
 *   <li>时间范围：按创建时间范围过滤。</li>
 * </ul>
 */
public interface ExecutionController {

    /**
     * 汇总数据
     *
     * @return
     */
    SummaryDataResponse summaryData();

    /**
     * 分页查询评测记录
     *
     * @param request
     * @return
     */
    Page<RecordListResponse> recordList(RecordListRequest request);

    /**
     * 导出评测记录
     *
     * @param response
     * @param request
     */
    void exportRecord(HttpServletResponse response, RecordListRequest request);
}
