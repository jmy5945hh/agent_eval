package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.BasePageRequest;
import com.example.agenteval.application.dto.request.agent.*;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.agent.AgentListResponse;
import com.example.agenteval.application.dto.response.agent.AgentVersionListResponse;
import com.example.agenteval.domain.service.AgentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    @PostMapping("")
    public ResponseEntity<CommonResponse<Void>> createAgent(
            @Valid @RequestBody AgentCreateRequest request) {
        log.info("Creating agent: name={}", request.getAgentName());
        agentDomainService.createAgent(request);
        return ResponseEntity.ok(CommonResponse.success());
    }

    /**
     * 新增版本信息
     *
     * @param agentId
     * @param request
     * @return
     */
    @PostMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> createAgentVersion(@PathVariable Integer agentId, @Valid @RequestBody AgentVersionCreateRequest request) {
        log.info("Creating agent version: agentId={},version={}", agentId, request.getVersion());
        agentDomainService.createAgentVersion(agentId, request);
        return ResponseEntity.ok(CommonResponse.success());
    }


    /**
     * 编辑 Agent 基本信息。
     */
    @PutMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> updateAgent(
            @PathVariable Integer agentId,
            @Valid @RequestBody AgentUpdateRequest request) {
        log.info("Updating agent: id={}, name={}", agentId, request.getAgentName());
        agentDomainService.updateAgent(agentId, request);
        return ResponseEntity.ok(CommonResponse.success());
    }

    /**
     * 编辑 Agent 版本基本信息。
     */
    @PutMapping("/{agentId}/version/{agentVersionId}")
    public ResponseEntity<CommonResponse<Void>> updateAgentVersion(
            @PathVariable Integer agentId, @PathVariable Integer agentVersionId,
            @Valid @RequestBody AgentVersionUpdateRequest request) {
        log.info("Updating agent: agent={}, agentVersionId={}", agentId, agentVersionId);
        agentDomainService.updateAgentVersion(agentId, agentVersionId, request);
        return ResponseEntity.ok(CommonResponse.success());
    }


    /**
     * 删除 Agent。若被测评任务引用则返回 409。
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> deleteAgent(@PathVariable Integer agentId) {
        log.info("Deleting agent: id={}", agentId);
        try {
            agentDomainService.deleteAgent(agentId);
            return ResponseEntity.ok(CommonResponse.success());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409).message(e.getMessage()).build());
        }
    }


    /**
     * 删除 Agent 版本。
     */
    @DeleteMapping("/versions/{agentVersionId}")
    public ResponseEntity<CommonResponse<Void>> deleteVersion(
            @PathVariable Integer agentVersionId) {
        log.info("Deleting agent version:  agentVersionId={}", agentVersionId);
        agentDomainService.deleteVersion(agentVersionId);
        return ResponseEntity.ok(CommonResponse.success());
    }

    /**
     * 查询agent列表
     *
     * @param request
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<CommonResponse<Page<AgentListResponse>>> agentList(@Valid AgentListRequest request) {
        return ResponseEntity.ok(CommonResponse.success(agentDomainService.agentList(request)));
    }

    /**
     * 根据Id获取agent信息
     *
     * @param agentId
     * @return
     */
    @PostMapping("/{agentId}/info")
    public ResponseEntity<CommonResponse<AgentListResponse>> agentInfo(@PathVariable Integer agentId) {
        return ResponseEntity.ok(CommonResponse.success(agentDomainService.agentInfo(agentId)));
    }

    /**
     * 根据id查询版本信息列表
     *
     * @param agentId
     * @return
     */
    @PostMapping("/{agentId}/version/list")
    public ResponseEntity<CommonResponse<Page<AgentVersionListResponse>>> agentVersionList(@PathVariable Integer agentId, @RequestBody BasePageRequest basePageRequest) {
        return ResponseEntity.ok(CommonResponse.success(agentDomainService.agentVersionList(agentId, basePageRequest)));
    }

    /**
     * 根据Id查询版本信息
     *
     * @param agentVersionId
     * @return
     */
    @PostMapping("/version/info/{agentVersionId}")
    public ResponseEntity<CommonResponse<AgentVersionListResponse>> agentVersionInfo(@PathVariable Integer agentVersionId) {
        return ResponseEntity.ok(CommonResponse.success(agentDomainService.agentVersionInfo(agentVersionId)));
    }
}
