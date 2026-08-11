package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.model.ModelConfigRequest;
import com.example.agenteval.application.dto.request.model.ModelListRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.model.ModelInfoResponse;
import com.example.agenteval.application.dto.response.model.ModelListResponse;
import com.example.agenteval.domain.model.ModelConfigPO;
import com.example.agenteval.domain.service.ModelConfigService;
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
 * 模型配置管理控制器，负责模型的新增、编辑、删除。
 * 只读列表查询由 ReferenceDataController 提供。
 *
 * <p>模型分为两类：测评模型（scoring=false，供 Agent 执行使用）和评分模型（scoring=true，供自动评分使用）。
 * API Key 需加密存储，查询列表时不返回完整 Key。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Api(tags = "模型配置")
@ApiSupport(order = 3)
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    /**
     * 新增模型配置。
     */
    @ApiOperation(value = "新增模型配置")
    @PostMapping("")
    public ResponseEntity<CommonResponse<Void>> createModel(
            @Valid @RequestBody ModelConfigRequest request) {
        log.info("Creating model: name={}, scoring={}", request.getModelName(), request.getScoring());
        modelConfigService.createModel(request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 编辑模型配置。若 authorization 字段为空或脱敏值则不更新 Key。
     */
    @ApiOperation(value = "修改模型配置")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> updateModel(
            @ApiParam(value = "模型配置ID", required = true) @PathVariable Integer id,
            @Valid @RequestBody ModelConfigRequest request) {
        log.info("Updating model: id={}, name={}", id, request.getModelName());
        ModelConfigPO updated = modelConfigService.updateModel(id, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 删除模型配置。被测评任务或评分任务引用时返回 409。
     */
    @ApiOperation(value = "删除模型配置")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteModel(@ApiParam(value = "模型配置ID", required = true) @PathVariable Integer id) {
        log.info("Deleting model: id={}", id);
        try {
            modelConfigService.deleteModel(id);
            return ResponseEntity.ok(CommonResponse.success(null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409).message(e.getMessage()).build());
        }
    }

    /**
     * 分页查询
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "分页查询模型列表")
    @PostMapping("/list")
    public ResponseEntity<CommonResponse<Page<ModelListResponse>>> modelList(@Valid @RequestBody ModelListRequest request) {
        Page<ModelListResponse> modelListResponses = modelConfigService.modelList(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(modelListResponses));
    }

    /**
     * 根据id查询模型信息
     *
     * @return
     */
    @ApiOperation(value = "根据id查询模型信息")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ModelInfoResponse>> modelInfo(@ApiParam(value = "模型配置ID", required = true) @PathVariable("id") Integer id) {
        return ResponseEntity.ok(CommonResponse.success(modelConfigService.modelInfo(id)));
    }
}
