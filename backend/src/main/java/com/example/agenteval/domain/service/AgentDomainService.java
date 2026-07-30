package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.AgentRequest;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.AgentVersionPO;

import java.util.List;

/**
 * Agent 领域服务接口 — 负责 Agent 产品及其版本的 CRUD。
 *
 * <h4>业务规则</h4>
 * <ul>
 *   <li>Agent name 唯一，用作业务标识。</li>
 *   <li>删除 Agent 前检查是否被测评任务引用，有则禁止删除。</li>
 *   <li>版本号在同一个 Agent 下唯一。</li>
 * </ul>
 */
public interface AgentDomainService {

    // ==================== Agent 管理 ====================

    /**
     * 新增 Agent 产品。
     *
     * @param request 包含 name、version、vendor、description、startCmd、executorType
     * @return 创建后的 Agent 实体
     */
    AgentInfoPO createAgent(AgentRequest request);

    /**
     * 编辑 Agent 产品信息。
     *
     * @param agentId Agent ID
     * @param request 编辑请求
     * @return 更新后的 Agent 实体
     */
    AgentInfoPO updateAgent(Long agentId, AgentRequest request);

    /**
     * 删除 Agent。
     * <p>若该 Agent 被测评任务引用，抛出异常禁止删除。</p>
     *
     * @param agentId Agent ID
     */
    void deleteAgent(Long agentId);

    // ==================== Agent 版本管理 ====================

    /**
     * 为指定 Agent 新增一个版本。
     *
     * @param agentId Agent ID
     * @param version 版本号
     * @param notes   版本说明
     * @param enabled 是否启用
     * @return 创建后的版本实体
     */
    AgentVersionPO addVersion(Long agentId, String version, String notes, Boolean enabled);

    /**
     * 编辑 Agent 版本信息。
     *
     * @param versionId 版本 ID
     * @param version   版本号
     * @param notes     版本说明
     * @param enabled   是否启用
     * @return 更新后的版本实体
     */
    AgentVersionPO updateVersion(Long versionId, String version, String notes, Boolean enabled);

    /**
     * 删除 Agent 版本。
     *
     * @param versionId 版本 ID
     */
    void deleteVersion(Long versionId);

    /**
     * 查询指定 Agent 的所有版本。
     *
     * @param agentId Agent ID
     * @return 版本列表
     */
    List<AgentVersionPO> getVersions(Long agentId);
}
