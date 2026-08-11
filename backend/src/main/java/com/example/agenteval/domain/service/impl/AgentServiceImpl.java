package com.example.agenteval.domain.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.example.agenteval.application.dto.BasePageRequest;
import com.example.agenteval.application.dto.request.agent.*;
import com.example.agenteval.application.dto.response.agent.AgentListResponse;
import com.example.agenteval.application.dto.response.agent.AgentVersionListResponse;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.AgentVersionPO;
import com.example.agenteval.domain.repository.AgentInfoPORespository;
import com.example.agenteval.domain.repository.AgentVersionPORespository;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.service.AgentService;
import com.example.agenteval.domain.service.OSService;
import com.example.agenteval.domain.service.mapstruct.AgentMapper;
import com.example.agenteval.infrastructure.util.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent 领域服务实现 — {@link AgentService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>Agent 产品 CRUD：创建、编辑、删除，保证 name 全局唯一。</li>
 *   <li>Agent 版本管理：为 Agent 添加/编辑/删除版本，保证版本号在 Agent 下唯一。</li>
 *   <li>删除依赖检查：删除 Agent 前校验是否被测评任务引用。</li>
 * </ul>
 *
 * @see AgentService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentInfoPORespository agentInfoRepository;
    private final AgentVersionPORespository agentVersionRepository;
    private final EvaluationTaskPORespository evaluationTaskRepository;
    private final OSService osService;
    private final AgentMapper agentMapper;

    // ==================== Agent 管理 ====================

    /**
     * 新增 Agent 产品。
     */
    @Override
    @Transactional
    public void createAgent(AgentCreateRequest request) {
        // name 全局唯一校验
        if (agentInfoRepository.existsByAgentName(request.getAgentName())) {
            throw new IllegalArgumentException("Agent 名称已存在: " + request.getAgentName());
        }

        AgentInfoPO agentInfoPO = AgentInfoPO.builder().agentName(request.getAgentName()).description(request.getDescription())
                .startCmd(request.getStartCmd()).enabled(MapUtil.mapBoolean(request.getEnabled(), true))
                .defaultAgent(MapUtil.mapBoolean(request.getDefaultAgent(), false))
                .configPath(request.getConfigPath()).build();
        AgentInfoPO saved = agentInfoRepository.save(agentInfoPO);
        if (request.getDefaultAgent()) {
            agentInfoRepository.updateDefaultAgentExceptId(agentInfoPO.getId(), MapUtil.mapBoolean(null, false));
        }
        log.info("Agent created: id={}, name={}", saved.getId(), saved.getAgentName());
    }


    @Override
    @Transactional
    public void createAgentVersion(Integer agentId, AgentVersionCreateRequest request) {
        AgentInfoPO agent = agentInfoRepository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));

        if (agentVersionRepository.existsByAgentIdAndVersion(agentId, request.getVersion())) {
            throw new IllegalArgumentException("Agent 版本已存在: " + request.getVersion());
        }

        String osFile = osService.createAndUploadFile(request.getConfigContent());
        try {
            AgentVersionPO agentVersionPO = AgentVersionPO.builder().agentId(agentId).version(request.getVersion()).notes(request.getNotes()).enabled(MapUtil.mapBoolean(request.getEnabled(), true)).contentOsPath(osFile).build();

            agentVersionRepository.save(agentVersionPO);
            if (request.getDefaultVersion()) {
                agent.setVersion(request.getVersion());
                agentInfoRepository.save(agent);
            }
        } catch (Exception e) {
            osService.deleteFile(osFile);
            log.error("insert agent version error delete os file :agentId={}, agentVersion={}, osFile={}", agentId, request.getVersion(), osFile);
            throw new RuntimeException("插入agent版本信息异常");
        }
        log.info("Agent version created: agentId={}, agentVersion={}", agentId, request.getVersion());
    }

    @Override
    @Transactional
    public void updateAgentVersion(Integer agentId, Integer agentVersionId, AgentVersionUpdateRequest request) {
        AgentInfoPO agent = agentInfoRepository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
        AgentVersionPO agentVersionPO = agentVersionRepository.findById(agentVersionId).orElseThrow(() -> new IllegalArgumentException("Agent 版本不存在: " + agentVersionId));

        // 若修改版本号，校验同一 Agent 下的唯一性
        if (StrUtil.isNotBlank(request.getVersion()) && !request.getVersion().equals(agentVersionPO.getVersion())) {
            if (agentVersionRepository.existsByAgentIdAndVersion(agentId, request.getVersion())) {
                throw new IllegalArgumentException("版本号 " + request.getVersion() + " 在该 Agent 下已存在");
            }
            agentVersionPO.setVersion(request.getVersion());
        }

        if (StrUtil.isNotBlank(request.getNotes())) {
            agentVersionPO.setNotes(request.getNotes());
        }

        String osFile = "";
        String oldFile = "";
        if (StrUtil.isNotBlank(agentVersionPO.getContentOsPath())) {
            oldFile = agentVersionPO.getContentOsPath();
            if (StrUtil.isNotBlank(request.getConfigContent())) {
                osFile = osService.createAndUploadFile(request.getConfigContent());
                agentVersionPO.setContentOsPath(osFile);
            }
        }
        if (ObjUtil.isNotNull(request.getEnabled())) {
            agentVersionPO.setEnabled(MapUtil.mapBoolean(request.getEnabled(), true));
        }
        try {
            agentVersionRepository.save(agentVersionPO);
            if (ObjUtil.isNotNull(request.getDefaultVersion()) && request.getDefaultVersion()) {
                agent.setVersion(request.getVersion());
                agentInfoRepository.save(agent);
            }
        } catch (Exception e) {
            log.error("update agent version error delete os file :agentId={}, agentVersion={}, osFile={}", agentId, request.getVersion(), osFile);
            throw new RuntimeException("更新agent版本信息异常");
        }
        if (StrUtil.isNotBlank(oldFile)) {
            osService.deleteFile(oldFile);
        }

    }

    @Override
    public Page<AgentListResponse> agentList(AgentListRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<AgentInfoPO> agentInfoPOS = agentInfoRepository.findByAgentName(request.getAgentName(), pageable);
        return agentInfoPOS.map(agentMapper::toListResponse);
    }

    @Override
    public AgentListResponse agentInfo(Integer agentId) {
        AgentInfoPO agent = agentInfoRepository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
        return agentMapper.toListResponse(agent);
    }

    @Override
    public Page<AgentVersionListResponse> agentVersionList(Integer agentId, BasePageRequest basePageRequest) {
        agentInfoRepository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(basePageRequest.getPage(), basePageRequest.getSize(), sort);
        Page<AgentVersionPO> agentVersionPOS = agentVersionRepository.findByAgentId(agentId, pageable);
        return agentVersionPOS.map(agentMapper::toListResponse);
    }

    @Override
    public AgentVersionListResponse agentVersionInfo(Integer agentVersionId) {
        AgentVersionPO agentVersionPO = agentVersionRepository.findById(agentVersionId).orElseThrow(() -> new IllegalArgumentException("Agent 版本不存在: " + agentVersionId));
        return agentMapper.toListResponse(agentVersionPO);
    }

    /**
     * 编辑 Agent 产品信息。
     * <p>若修改了 name，需校验新 name 未被其他 Agent 占用。</p>
     */
    @Override
    @Transactional
    public void updateAgent(Integer agentId, AgentUpdateRequest request) {
        AgentInfoPO agent = agentInfoRepository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));

        // 若修改了 name，校验唯一性（排除自身）
        if (StrUtil.isNotBlank(request.getAgentName()) && !request.getAgentName().equals(agent.getAgentName())) {
            if (agentInfoRepository.existsByAgentName(request.getAgentName())) {
                throw new IllegalArgumentException("Agent 名称已存在: " + request.getAgentName());
            }
        }
        if (StrUtil.isNotBlank(request.getAgentName())) {
            agent.setAgentName(request.getAgentName());
        }
        if (StrUtil.isNotBlank(request.getDescription())) {
            agent.setDescription(request.getDescription());
        }
        if (StrUtil.isNotBlank(request.getStartCmd())) {
            agent.setStartCmd(request.getStartCmd());
        }
        if (ObjUtil.isNotNull(request.getEnabled())) {
            agent.setEnabled(MapUtil.mapBoolean(request.getEnabled(), true));
        }
        if (StrUtil.isNotBlank(request.getConfigPath())) {
            agent.setConfigPath(request.getConfigPath());
        }
        if (ObjUtil.isNotNull(request.getDefaultAgent())) {
            agent.setDefaultAgent(MapUtil.mapBoolean(request.getDefaultAgent(), false));
        }
        AgentInfoPO saved = agentInfoRepository.save(agent);
        if (ObjUtil.isNotNull(request.getDefaultAgent()) && request.getDefaultAgent()) {
            agentInfoRepository.updateDefaultAgentExceptId(agent.getId(), MapUtil.mapBoolean(null, false));
        }
        log.info("Agent updated: id={}, name={}", agentId, saved.getAgentName());
    }

    /**
     * 删除 Agent。
     * <p>若该 Agent 被测评任务引用，抛出 {@link IllegalStateException} 禁止删除。
     * 删除 Agent 时会同时清理其所有版本记录。</p>
     */
    @Override
    @Transactional
    public void deleteAgent(Integer agentId) {
        // 检查是否被测评任务引用
        if (evaluationTaskRepository.existsByAgentId(agentId)) {
            throw new IllegalStateException("该 Agent 已被测评任务引用，无法删除");
        }

        // 清理关联的版本记录
        List<AgentVersionPO> versions = agentVersionRepository.findByAgentId(agentId);
        if (!versions.isEmpty()) {
            agentVersionRepository.deleteAll(versions);
            log.info("Deleted {} versions for agent: {}", versions.size(), agentId);
        }

        agentInfoRepository.deleteById(agentId);
        log.info("Agent deleted: id={}", agentId);
    }

    /**
     * 删除 Agent 版本（物理删除）。
     */
    @Override
    @Transactional
    public void deleteVersion(Integer agentVersionId) {
        if (!agentVersionRepository.existsById(agentVersionId)) {
            throw new IllegalArgumentException("版本不存在: " + agentVersionId);
        }
        if (evaluationTaskRepository.existsByAgentVersionId(agentVersionId)) {
            throw new IllegalStateException("该 Agent 版本已被测评任务引用，无法删除");
        }
        agentVersionRepository.deleteById(agentVersionId);
        log.info("Version deleted: id={}", agentVersionId);
    }


}
