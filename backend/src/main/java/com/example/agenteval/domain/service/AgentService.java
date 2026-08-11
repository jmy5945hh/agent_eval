package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.BasePageRequest;
import com.example.agenteval.application.dto.request.agent.*;
import com.example.agenteval.application.dto.response.agent.AgentListResponse;
import com.example.agenteval.application.dto.response.agent.AgentVersionListResponse;
import org.springframework.data.domain.Page;

import javax.validation.Valid;

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
public interface AgentService {

    // ==================== Agent 管理 ====================

    /**
     * 新增 Agent 产品。
     *
     * @param request 包含 name、version、vendor、description、startCmd、executorType
     * @return 创建后的 Agent 实体
     */
    void createAgent(AgentCreateRequest request);

    /**
     * 编辑 Agent 产品信息。
     *
     * @param agentId Agent ID
     * @param request 编辑请求
     * @return 更新后的 Agent 实体
     */
    void updateAgent(Integer agentId, AgentUpdateRequest request);

    /**
     * 删除 Agent。
     * <p>若该 Agent 被测评任务引用，抛出异常禁止删除。</p>
     *
     * @param agentId Agent ID
     */
    void deleteAgent(Integer agentId);


    /**
     * 删除 Agent 版本。
     *
     * @param agentVersionId 版本 ID
     */
    void deleteVersion(Integer agentVersionId);


    /**
     * 新增agent版本信息
     *
     * @param agentId
     * @param request
     */
    void createAgentVersion(Integer agentId, @Valid AgentVersionCreateRequest request);

    /**
     * 更新版本信息
     *
     * @param agentId
     * @param agentVersionId
     * @param request
     */
    void updateAgentVersion(Integer agentId, Integer agentVersionId, AgentVersionUpdateRequest request);

    /**
     * 查询agent列表
     *
     * @param request
     * @return
     */
    Page<AgentListResponse> agentList(AgentListRequest request);

    /**
     * 根据id获取agent信息
     *
     * @param agentId
     * @return
     */
    AgentListResponse agentInfo(Integer agentId);

    /**
     * 分页查询agent版本列表
     *
     * @param agentId
     * @return
     */
    Page<AgentVersionListResponse> agentVersionList(Integer agentId, BasePageRequest basePageRequest);

    /**
     * 根据Id查询版本信息
     *
     * @param agentVersionId
     * @return
     */
    AgentVersionListResponse agentVersionInfo(Integer agentVersionId);
}
