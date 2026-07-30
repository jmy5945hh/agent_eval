package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.ModelConfigRequest;
import com.example.agenteval.domain.model.ModelConfigPO;
import com.example.agenteval.domain.service.ModelConfigDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ModelConfigController {

    private final ModelConfigDomainService modelConfigDomainService;

    /**
     * 新增模型配置。
     */
    @PostMapping
    public ResponseEntity<CommonResponse<ModelConfigPO>> createModel(
            @Valid @RequestBody ModelConfigRequest request) {
        log.info("Creating model: name={}, scoring={}", request.getName(), request.getScoring());
        ModelConfigPO created = modelConfigDomainService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(created));
    }

    /**
     * 编辑模型配置。若 authorization 字段为空或脱敏值则不更新 Key。
     */
    @PutMapping("/{id}")
    public CommonResponse<ModelConfigPO> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelConfigRequest request) {
        log.info("Updating model: id={}, name={}", id, request.getName());
        ModelConfigPO updated = modelConfigDomainService.updateModel(id, request);
        return CommonResponse.success(updated);
    }

    /**
     * 删除模型配置。被测评任务或评分任务引用时返回 409。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteModel(@PathVariable Long id) {
        log.info("Deleting model: id={}", id);
        try {
            modelConfigDomainService.deleteModel(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409).message(e.getMessage()).build());
        }
    }
}
