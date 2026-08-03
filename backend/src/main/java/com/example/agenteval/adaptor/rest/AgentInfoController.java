package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.AgentRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.AgentVersionPO;
import com.example.agenteval.domain.service.AgentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Agent 管理控制器，负责 Agent 的新增、编辑、删除及版本管理。
 * 只读查询（列表、版本列表）由 ReferenceDataController 提供。
 */
@Slf4j
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentInfoController {

    private final AgentDomainService agentDomainService;

    /**
     * 新增 Agent 产品。name 必填且全局唯一，status 默认为 enabled。
     */
    @PostMapping
    public ResponseEntity<CommonResponse<AgentInfoPO>> createAgent(
            @Valid @RequestBody AgentRequest request) {
        log.info("Creating agent: name={}, executorType={}", request.getName(), request.getExecutorType());
        AgentInfoPO created = agentDomainService.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(created));
    }

    /**
     * 编辑 Agent 基本信息。
     */
    @PutMapping("/{agentId}")
    public CommonResponse<AgentInfoPO> updateAgent(
            @PathVariable Long agentId,
            @Valid @RequestBody AgentRequest request) {
        log.info("Updating agent: id={}, name={}", agentId, request.getName());
        AgentInfoPO updated = agentDomainService.updateAgent(agentId, request);
        return CommonResponse.success(updated);
    }

    /**
     * 删除 Agent。若被测评任务引用则返回 409。
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> deleteAgent(@PathVariable Long agentId) {
        log.info("Deleting agent: id={}", agentId);
        try {
            agentDomainService.deleteAgent(agentId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409).message(e.getMessage()).build());
        }
    }

    /**
     * 为指定 Agent 新增一个版本。版本号在同一 Agent 下唯一。
     */
    @PostMapping("/{agentId}/versions")
    public ResponseEntity<CommonResponse<AgentVersionPO>> addVersion(
            @PathVariable Long agentId,
            @RequestBody Map<String, Object> body) {
        String version = (String) body.get("version");
        String notes = (String) body.getOrDefault("notes", "");
        Boolean enabled = body.containsKey("enabled")
                ? (Boolean) body.get("enabled") : true;

        log.info("Adding version {} to agent {}", version, agentId);
        AgentVersionPO created = agentDomainService.addVersion(agentId, version, notes, enabled);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(created));
    }

    /**
     * 编辑 Agent 版本信息，支持修改版本号、版本说明和启用状态。
     */
    @PutMapping("/{agentId}/versions/{versionId}")
    public CommonResponse<AgentVersionPO> updateVersion(
            @PathVariable Long agentId,
            @PathVariable Long versionId,
            @RequestBody Map<String, Object> body) {
        String version = (String) body.get("version");
        String notes = (String) body.getOrDefault("notes", "");
        Boolean enabled = body.containsKey("enabled")
                ? (Boolean) body.get("enabled") : null;

        log.info("Updating agent version: agentId={}, versionId={}", agentId, versionId);
        AgentVersionPO updated = agentDomainService.updateVersion(versionId, version, notes, enabled);
        return CommonResponse.success(updated);
    }

    /**
     * 删除 Agent 版本。
     */
    @DeleteMapping("/{agentId}/versions/{versionId}")
    public ResponseEntity<CommonResponse<Void>> deleteVersion(
            @PathVariable Long agentId,
            @PathVariable Long versionId) {
        log.info("Deleting agent version: agentId={}, versionId={}", agentId, versionId);
        agentDomainService.deleteVersion(versionId);
        return ResponseEntity.noContent().build();
    }
}
