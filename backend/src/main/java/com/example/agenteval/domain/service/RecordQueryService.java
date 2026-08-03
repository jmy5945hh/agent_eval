package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.response.task.TaskResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

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
public interface RecordQueryService {

    /**
     * 分页查询历史测评记录。
     * <p>支持多条件筛选（agentId、modelId、status、时间范围），
     * 按创建时间倒序排列。</p>
     *
     * @param page     页码（从 1 开始）
     * @param size     每页条数
     * @param agentId  Agent ID 筛选（可选）
     * @param modelId  模型 ID 筛选（可选）
     * @param status   任务状态筛选（可选）
     * @param dateFrom 开始时间（可选）
     * @param dateTo   结束时间（可选）
     * @return 分页结果
     */
    Page<TaskResponse> listRecords(int page, int size,
                                   String agentId, String modelId,
                                   String status,
                                   LocalDateTime dateFrom, LocalDateTime dateTo);

    /**
     * 查询测评记录详情。
     * <p>聚合任务基本信息、执行统计（成功率/耗时/Token）、
     * 评分汇总（各维度平均分/总分）。</p>
     *
     * @param taskId 任务 ID
     * @return 任务详情（含 runs、统计汇总）
     */
    TaskResponse getRecordDetail(Long taskId);
}
