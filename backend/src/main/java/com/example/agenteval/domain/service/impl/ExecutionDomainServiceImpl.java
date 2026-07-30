package com.example.agenteval.domain.service.impl;

import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.TrajectoryEntry;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import com.example.agenteval.domain.service.ExecutionDomainService;
import com.example.agenteval.infrastructure.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行领域服务实现 — {@link ExecutionDomainService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>执行记录详情查询：从 JPA 实体读取状态、统计信息及错误信息。</li>
 *   <li>执行轨迹查询：从对象存储按需加载轨迹数据。</li>
 *   <li>轨迹追加：执行过程中实时写入轨迹条目到对象存储。</li>
 * </ul>
 *
 * <h4>数据存储说明</h4>
 * <p>执行轨迹（{@link TrajectoryEntry} 列表）序列化为 JSON 存储在对象存储中，
 * 追加操作采用「下载 → 追加 → 上传」策略，保证轨迹完整性。</p>
 *
 * @see ExecutionDomainService
 * @see ObjectStorageService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionDomainServiceImpl implements ExecutionDomainService {

    private final TaskCaseRunPORespository caseRunRepository;
    private final ObjectStorageService storageService;

    // ==================== 执行记录详情 ====================

    /**
     * 查询单条执行记录详情。
     * <p>直接从 JPA 实体读取，包含状态、轮次、Token、耗时等统计字段。
     * 失败时可通过 {@link TaskCaseRunPO#getErrorInfoKey()} 获取错误信息对象存储键值。</p>
     */
    @Override
    public TaskCaseRunPO getRunDetail(Long runId) {
        return caseRunRepository.findById(runId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + runId));
    }

    // ==================== 轨迹查询与记录 ====================

    /**
     * 查询单条执行记录的完整轨迹。
     * <p>从对象存储中加载轨迹 JSON 并反序列化为 {@link TrajectoryEntry} 列表，
     * 按写入顺序排列。若轨迹不存在则返回空列表。</p>
     */
    @Override
    public List<TrajectoryEntry> getTrajectory(Long runId) {
        TaskCaseRunPO run = getRunDetail(runId);
        String key = run.getTrajectoryKey();
        if (key == null || key.isEmpty()) {
            log.debug("No trajectory found for run {}", runId);
            return new ArrayList<>();
        }
        List<TrajectoryEntry> trajectory = storageService.downloadJsonList(key, TrajectoryEntry.class);
        if (trajectory == null) {
            log.warn("Failed to load trajectory for run {}, returning empty list", runId);
            return new ArrayList<>();
        }
        log.debug("Loaded {} trajectory entries for run {}", trajectory.size(), runId);
        return trajectory;
    }

    /**
     * 采集并持久化一条轨迹条目。
     * <p>在 Agent 执行过程中被回调。采用「下载 → 追加 → 上传」策略：</p>
     * <ol>
     *   <li>从对象存储加载现有轨迹列表；</li>
     *   <li>将新条目追加到列表末尾；</li>
     *   <li>将更新后的列表上传回对象存储；</li>
     * </ol>
     */
    @Override
    @Transactional
    public void appendTrajectory(Long runId, TrajectoryEntry entry) {
        TaskCaseRunPO run = caseRunRepository.findById(runId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + runId));

        // 加载现有轨迹
        List<TrajectoryEntry> trajectory = getTrajectory(runId);

        // 追加新条目
        trajectory.add(entry);

        // 上传更新后的轨迹列表
        String key = buildTrajectoryKey(runId);
        String uri = storageService.uploadJsonList(key, trajectory);
        run.setTrajectoryKey(uri);
        caseRunRepository.save(run);

        log.debug("Appended trajectory entry [role={}, kind={}] to run {} (total: {})",
                entry.getRole(), entry.getKind(), runId, trajectory.size());
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建轨迹对象存储键值。
     *
     * @param runId 执行记录 ID
     * @return 对象存储 key（如 trajectory/42/trajectory.json）
     */
    private String buildTrajectoryKey(Long runId) {
        return "trajectory/" + runId + "/trajectory.json";
    }
}
