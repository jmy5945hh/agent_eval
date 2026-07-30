package com.example.agenteval.domain.service;

import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.TrajectoryEntry;

import java.util.List;

/**
 * 执行领域服务接口 — 负责执行记录详情、轨迹数据的查询。
 *
 * <h4>数据来源</h4>
 * <ul>
 *   <li>执行记录（TaskCaseRun）：来自 JPA 实体，含状态、轮次、Token、耗时等统计。</li>
 *   <li>执行轨迹（TrajectoryEntry）：来自对象存储或数据库，按 seq 排序。</li>
 * </ul>
 */
public interface ExecutionDomainService {

    /**
     * 查询单条执行记录详情。
     * <p>包含状态、统计信息（rounds, tokensIn, tokensOut, durationMs）、
     * 失败时的错误信息（error_category, error_log）。</p>
     *
     * @param runId 执行记录 ID
     * @return 执行记录实体
     */
    TaskCaseRunPO getRunDetail(Long runId);

    /**
     * 查询单条执行记录的完整轨迹。
     * <p>按 seq 升序排列，记录执行过程中的每一条人机对话。</p>
     *
     * @param runId 执行记录 ID
     * @return 轨迹条目列表（role=user/agent/tool）
     */
    List<TrajectoryEntry> getTrajectory(Long runId);

    /**
     * 采集并持久化一条轨迹条目。
     * <p>在 Agent 执行过程中被回调，实时写入对象存储或数据库。</p>
     *
     * @param runId  执行记录 ID
     * @param entry  轨迹条目
     */
    void appendTrajectory(Long runId, TrajectoryEntry entry);
}
