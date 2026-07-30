package com.example.agenteval.domain.service.impl;

import com.example.agenteval.application.dto.AgentRequest;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.AgentVersionPO;
import com.example.agenteval.domain.repository.AgentInfoPORespository;
import com.example.agenteval.domain.repository.AgentVersionPORespository;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.service.AgentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent 领域服务实现 — {@link AgentDomainService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>Agent 产品 CRUD：创建、编辑、删除，保证 name 全局唯一。</li>
 *   <li>Agent 版本管理：为 Agent 添加/编辑/删除版本，保证版本号在 Agent 下唯一。</li>
 *   <li>删除依赖检查：删除 Agent 前校验是否被测评任务引用。</li>
 * </ul>
 *
 * @see AgentDomainService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDomainServiceImpl implements AgentDomainService {

    private final AgentInfoPORespository agentInfoRepository;
    private final AgentVersionPORespository agentVersionRepository;
    private final EvaluationTaskPORespository evaluationTaskRepository;

    // ==================== Agent 管理 ====================

    /**
     * 新增 Agent 产品。
     * <p>校验 name 全局唯一，status 默认 enabled（值为 1）。</p>
     */
    @Override
    @Transactional
    public AgentInfoPO createAgent(AgentRequest request) {
        // name 全局唯一校验
        if (agentInfoRepository.existsByAgentName(request.getName())) {
            throw new IllegalArgumentException("Agent 名称已存在: " + request.getName());
        }

        AgentInfoPO agent = AgentInfoPO.builder()
                .agentName(request.getName())
                .version(request.getVersion())
                .description(request.getDescription())
                .startCmd(request.getStartCmd())
                .enabled(mapStatus(request.getStatus()))
                .build();

        AgentInfoPO saved = agentInfoRepository.save(agent);
        log.info("Agent created: id={}, name={}", saved.getId(), saved.getAgentName());
        return saved;
    }

    /**
     * 编辑 Agent 产品信息。
     * <p>若修改了 name，需校验新 name 未被其他 Agent 占用。</p>
     */
    @Override
    @Transactional
    public AgentInfoPO updateAgent(Long agentId, AgentRequest request) {
        AgentInfoPO agent = agentInfoRepository.findById(agentId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));

        // 若修改了 name，校验唯一性（排除自身）
        if (!request.getName().equals(agent.getAgentName())) {
            if (agentInfoRepository.existsByAgentName(request.getName())) {
                throw new IllegalArgumentException("Agent 名称已存在: " + request.getName());
            }
        }

        agent.setAgentName(request.getName());
        agent.setVersion(request.getVersion());
        agent.setDescription(request.getDescription());
        agent.setStartCmd(request.getStartCmd());
        if (request.getStatus() != null) {
            agent.setEnabled(mapStatus(request.getStatus()));
        }

        AgentInfoPO saved = agentInfoRepository.save(agent);
        log.info("Agent updated: id={}, name={}", agentId, saved.getAgentName());
        return saved;
    }

    /**
     * 删除 Agent。
     * <p>若该 Agent 被测评任务引用，抛出 {@link IllegalStateException} 禁止删除。
     * 删除 Agent 时会同时清理其所有版本记录。</p>
     */
    @Override
    @Transactional
    public void deleteAgent(Long agentId) {
        // 检查是否被测评任务引用
        if (evaluationTaskRepository.existsByAgentId(agentId.intValue())) {
            throw new IllegalStateException("该 Agent 已被测评任务引用，无法删除");
        }

        // 清理关联的版本记录
        List<AgentVersionPO> versions = agentVersionRepository.findByAgentId(agentId.intValue());
        if (!versions.isEmpty()) {
            agentVersionRepository.deleteAll(versions);
            log.info("Deleted {} versions for agent: {}", versions.size(), agentId);
        }

        agentInfoRepository.deleteById(agentId.intValue());
        log.info("Agent deleted: id={}", agentId);
    }

    // ==================== Agent 版本管理 ====================

    /**
     * 为指定 Agent 新增一个版本。
     * <p>校验 Agent 存在性及版本号在同一 Agent 下的唯一性。
     * enabled 为 null 时默认启用（值为 1）。</p>
     */
    @Override
    @Transactional
    public AgentVersionPO addVersion(Long agentId, String version, String notes, Boolean enabled) {
        // 校验 Agent 存在
        agentInfoRepository.findById(agentId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));

        // 校验版本号唯一性
        if (agentVersionRepository.existsByAgentIdAndVersion(agentId.intValue(), version)) {
            throw new IllegalArgumentException(
                    "版本号 " + version + " 在该 Agent 下已存在");
        }

        AgentVersionPO entity = AgentVersionPO.builder()
                .agentId(agentId.intValue())
                .version(version)
                .notes(notes != null ? notes : "")
                .enabled(mapEnabled(enabled))
                .build();

        AgentVersionPO saved = agentVersionRepository.save(entity);
        log.info("Version added: id={}, agentId={}, version={}", saved.getId(), agentId, version);
        return saved;
    }

    /**
     * 编辑 Agent 版本信息。
     * <p>若修改了版本号，需校验新版本号未被同 Agent 下其他版本占用。
     * 各字段为 null 时保持原值不变。</p>
     */
    @Override
    @Transactional
    public AgentVersionPO updateVersion(Long versionId, String version, String notes, Boolean enabled) {
        AgentVersionPO entity = agentVersionRepository.findById(versionId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("版本不存在: " + versionId));

        // 若修改版本号，校验同一 Agent 下的唯一性
        if (version != null && !version.equals(entity.getVersion())) {
            if (agentVersionRepository.existsByAgentIdAndVersion(entity.getAgentId(), version)) {
                throw new IllegalArgumentException(
                        "版本号 " + version + " 在该 Agent 下已存在");
            }
            entity.setVersion(version);
        }

        if (notes != null) {
            entity.setNotes(notes);
        }
        if (enabled != null) {
            entity.setEnabled(mapEnabled(enabled));
        }

        AgentVersionPO saved = agentVersionRepository.save(entity);
        log.info("Version updated: id={}, version={}", versionId, saved.getVersion());
        return saved;
    }

    /**
     * 删除 Agent 版本（物理删除）。
     */
    @Override
    @Transactional
    public void deleteVersion(Long versionId) {
        if (!agentVersionRepository.existsById(versionId.intValue())) {
            throw new IllegalArgumentException("版本不存在: " + versionId);
        }
        agentVersionRepository.deleteById(versionId.intValue());
        log.info("Version deleted: id={}", versionId);
    }

    /**
     * 查询指定 Agent 的所有版本，按创建时间排序（JPA 默认）。
     */
    @Override
    public List<AgentVersionPO> getVersions(Long agentId) {
        // 校验 Agent 存在
        agentInfoRepository.findById(agentId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
        return agentVersionRepository.findByAgentId(agentId.intValue());
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 status 字符串映射为 byte。
     * <ul>
     *   <li>enabled / null → 1</li>
     *   <li>disabled → 0</li>
     * </ul>
     */
    private byte mapStatus(String status) {
        if (status == null || "enabled".equalsIgnoreCase(status)) {
            return 1;
        }
        return 0;
    }

    /**
     * 将 Boolean enabled 映射为 byte。
     * <ul>
     *   <li>true / null → 1（默认启用）</li>
     *   <li>false → 0</li>
     * </ul>
     */
    private byte mapEnabled(Boolean enabled) {
        if (enabled == null || enabled) {
            return 1;
        }
        return 0;
    }
}
