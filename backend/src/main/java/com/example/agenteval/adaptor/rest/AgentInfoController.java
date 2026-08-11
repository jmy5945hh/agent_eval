package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.BasePageRequest;
import com.example.agenteval.application.dto.request.agent.*;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.agent.AgentListResponse;
import com.example.agenteval.application.dto.response.agent.AgentVersionListResponse;
import com.example.agenteval.domain.service.AgentService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(tags = "Agent配置控制器")
@ApiSupport(order = 2)
public class AgentInfoController {

    private final AgentService agentService;

    /**
     * 新增 Agent 产品。name 必填且全局唯一，status 默认为 enabled。
     */
    @ApiOperation(value = "新增Agent产品")
    @PostMapping("")
    public ResponseEntity<CommonResponse<Void>> createAgent(
            @Valid @RequestBody AgentCreateRequest request) {
        log.info("Creating agent: name={}", request.getAgentName());
        agentService.createAgent(request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 新增版本信息
     *
     * @param agentId
     * @param request
     * @return
     */
    @ApiOperation(value = "新增Agent版本")
    @PostMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> createAgentVersion(@ApiParam(value = "agent的id", required = true) @PathVariable Integer agentId, @Valid @RequestBody AgentVersionCreateRequest request) {
        log.info("Creating agent version: agentId={},version={}", agentId, request.getVersion());
        agentService.createAgentVersion(agentId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }


    /**
     * 编辑 Agent 基本信息。
     */
    @ApiOperation(value = "修改Agent基本信息")
    @PutMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> updateAgent(
            @ApiParam(value = "agent的id", required = true) @PathVariable Integer agentId,
            @Valid @RequestBody AgentUpdateRequest request) {
        log.info("Updating agent: id={}, name={}", agentId, request.getAgentName());
        agentService.updateAgent(agentId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 编辑 Agent 版本基本信息。
     */
    @ApiOperation(value = "修改Agent版本基本信息")
    @PutMapping("/{agentId}/version/{agentVersionId}")
    public ResponseEntity<CommonResponse<Void>> updateAgentVersion(
            @ApiParam(value = "agent的id", required = true) @PathVariable Integer agentId, @ApiParam(value = "agent版本的id", required = true) @PathVariable Integer agentVersionId,
            @Valid @RequestBody AgentVersionUpdateRequest request) {
        log.info("Updating agent: agent={}, agentVersionId={}", agentId, agentVersionId);
        agentService.updateAgentVersion(agentId, agentVersionId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }


    /**
     * 删除 Agent。若被测评任务引用则返回 409。
     */
    @ApiOperation(value = "删除Agent")
    @DeleteMapping("/{agentId}")
    public ResponseEntity<CommonResponse<Void>> deleteAgent(@ApiParam(value = "agent的id", required = true) @PathVariable Integer agentId) {
        log.info("Deleting agent: id={}", agentId);
        try {
            agentService.deleteAgent(agentId);
            return ResponseEntity.ok(CommonResponse.success(null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409).message(e.getMessage()).build());
        }
    }


    /**
     * 删除 Agent 版本。
     */
    @ApiOperation(value = "删除Agent版本")
    @DeleteMapping("/versions/{agentVersionId}")
    public ResponseEntity<CommonResponse<Void>> deleteVersion(
            @ApiParam(value = "agent版本的id", required = true) @PathVariable Integer agentVersionId) {
        log.info("Deleting agent version:  agentVersionId={}", agentVersionId);
        agentService.deleteVersion(agentVersionId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 查询agent列表
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "查询agent列表")
    @PostMapping("/list")
    public ResponseEntity<CommonResponse<Page<AgentListResponse>>> agentList(@Valid AgentListRequest request) {
        return ResponseEntity.ok(CommonResponse.success(agentService.agentList(request)));
    }

    /**
     * 根据Id获取agent信息
     *
     * @param agentId
     * @return
     */
    @ApiOperation(value = "根据Id获取agent信息")
    @PostMapping("/{agentId}/info")
    public ResponseEntity<CommonResponse<AgentListResponse>> agentInfo(@ApiParam(value = "agent的id", required = true) @PathVariable Integer agentId) {
        return ResponseEntity.ok(CommonResponse.success(agentService.agentInfo(agentId)));
    }

    /**
     * 根据id查询版本信息列表
     *
     * @param agentId
     * @return
     */
    @ApiOperation(value = "根据id查询agent版本信息列表")
    @PostMapping("/{agentId}/version/list")
    public ResponseEntity<CommonResponse<Page<AgentVersionListResponse>>> agentVersionList(@ApiParam(value = "agent的id", required = true) @PathVariable Integer agentId, @RequestBody BasePageRequest basePageRequest) {
        return ResponseEntity.ok(CommonResponse.success(agentService.agentVersionList(agentId, basePageRequest)));
    }

    /**
     * 根据Id查询版本信息
     *
     * @param agentVersionId
     * @return
     */
    @ApiOperation(value = "根据Id查询agent版本信息")
    @PostMapping("/version/info/{agentVersionId}")
    public ResponseEntity<CommonResponse<AgentVersionListResponse>> agentVersionInfo(@ApiParam(value = "agent版本的id", required = true) @PathVariable Integer agentVersionId) {
        return ResponseEntity.ok(CommonResponse.success(agentService.agentVersionInfo(agentVersionId)));
    }
}
