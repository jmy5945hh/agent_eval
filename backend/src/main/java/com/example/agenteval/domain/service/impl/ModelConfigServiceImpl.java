package com.example.agenteval.domain.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.example.agenteval.application.dto.request.model.ModelConfigRequest;
import com.example.agenteval.application.dto.request.model.ModelListRequest;
import com.example.agenteval.application.dto.response.model.ModelInfoResponse;
import com.example.agenteval.application.dto.response.model.ModelListResponse;
import com.example.agenteval.domain.model.ModelConfigPO;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.ModelConfigPORespository;
import com.example.agenteval.domain.service.ModelConfigService;
import com.example.agenteval.domain.service.OSService;
import com.example.agenteval.domain.service.mapstruct.ModelConfigMapper;
import com.example.agenteval.infrastructure.enums.ModelCallTypeEnum;
import com.example.agenteval.infrastructure.util.EnumUtil;
import com.example.agenteval.infrastructure.util.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模型配置领域服务实现 — {@link ModelConfigService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>模型 CRUD：新增、编辑、删除模型配置，保证名称唯一。</li>
 *   <li>删除依赖检查：删除前校验模型是否被测评任务或评分任务引用。</li>
 *   <li>API Key 脱敏：查询时不在日志中输出完整 authorization。</li>
 * </ul>
 *
 * <h4>模型分类</h4>
 * <p>模型分为两类：</p>
 * <ul>
 *   <li><b>测评模型</b>（scoring=0）：供 Agent 执行时调用，生成代码。</li>
 *   <li><b>评分模型</b>（scoring=1）：供评分阶段调用，对执行结果评定维度分。</li>
 * </ul>
 *
 * @see ModelConfigService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

    private final ModelConfigPORespository modelConfigRepository;
    private final EvaluationTaskPORespository evaluationTaskRepository;
    private final ModelConfigMapper modelConfigMapper;
    private final OSService osService;

    // ==================== 模型 CRUD ====================

    /**
     * 新增模型配置。
     * <p>校验名称全局唯一，默认 enabled=1、scoring=0（测评模型）。
     * authorization 明文传入，直接存储。</p>
     */
    @Override
    @Transactional
    public ModelConfigPO createModel(ModelConfigRequest request) {
        // 名称唯一性校验
        if (modelConfigRepository.existsByModelName(request.getModelName())) {
            throw new IllegalArgumentException("模型名称已存在: " + request.getModelName());
        }
        //校验类型
        ModelCallTypeEnum enumByField = EnumUtil.findEnumByField(ModelCallTypeEnum.class, ModelCallTypeEnum.TYPE_STATIC_NAME, request.getModelType());
        if (ObjUtil.isNull(enumByField)) {
            throw new IllegalArgumentException("模型类型不正确：" + request.getModelType());
        }

        ModelConfigPO model = ModelConfigPO.builder()
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .endpoint(request.getEndpoint())
                .authorization(request.getAuthorization())
                .version(request.getVersion())
                .description(request.getDescription())
                .enabled(MapUtil.mapBoolean(request.getEnabled(), true))
                .scoring(MapUtil.mapBoolean(request.getScoring(), false))
                .build();

        ModelConfigPO saved = modelConfigRepository.save(model);
        log.info("Model created: id={}, name={}, scoring={}",
                saved.getId(), saved.getModelName(), saved.getScoring());
        return saved;
    }

    /**
     * 编辑模型配置。
     * <p>各字段为 null 时保持原值不变。若 authorization 为空或脱敏占位符，不更新 Key。</p>
     */
    @Override
    @Transactional
    public ModelConfigPO updateModel(Integer id, ModelConfigRequest request) {
        ModelConfigPO model = modelConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在: " + id));

        // 若修改了名称，校验唯一性（排除自身）
        if (request.getModelName() != null && !request.getModelName().equals(model.getModelName())) {
            if (modelConfigRepository.existsByModelName(request.getModelName())) {
                throw new IllegalArgumentException("模型名称已存在: " + request.getModelName());
            }
            model.setModelName(request.getModelName());
        }

        //校验类型
        ModelCallTypeEnum enumByField = EnumUtil.findEnumByField(ModelCallTypeEnum.class, ModelCallTypeEnum.TYPE_STATIC_NAME, request.getModelType());
        if (ObjUtil.isNull(enumByField)) {
            throw new IllegalArgumentException("模型类型不正确：" + request.getModelType());
        }

        if (request.getModelType() != null) {
            model.setModelType(request.getModelType());
        }
        if (request.getEndpoint() != null) {
            model.setEndpoint(request.getEndpoint());
        }
        // authorization: 仅在非空且非脱敏占位符时更新
        if (request.getAuthorization() != null
                && !request.getAuthorization().isEmpty()
                && !isMasked(request.getAuthorization())) {
            model.setAuthorization(request.getAuthorization());
        }
        if (request.getVersion() != null) {
            model.setVersion(request.getVersion());
        }
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            model.setEnabled(MapUtil.mapBoolean(request.getEnabled(), true));
        }
        if (request.getScoring() != null) {
            model.setScoring(MapUtil.mapBoolean(request.getScoring(), false));
        }

        ModelConfigPO saved = modelConfigRepository.save(model);
        log.info("Model updated: id={}, name={}, scoring={}", id, saved.getModelName(), saved.getScoring());
        return saved;
    }

    /**
     * 删除模型配置。
     * <p>检查是否被测评任务（作为执行模型或评分模型）引用，有引用则抛出
     * {@link IllegalStateException}，由 Controller 返回 409。</p>
     */
    @Override
    @Transactional
    public void deleteModel(Integer id) {
        // 检查是否被测评任务引用（作为执行模型）
        if (evaluationTaskRepository.existsByModelId(id)) {
            throw new IllegalStateException("该模型已被测评任务引用，无法删除");
        }

        // 检查是否被评分任务引用（作为评分模型）
        if (evaluationTaskRepository.existsByScoringModelId(id)) {
            throw new IllegalStateException("该模型已被评分任务引用，无法删除");
        }

        modelConfigRepository.deleteById(id);
        log.info("Model deleted: id={}", id);
    }

    @Override
    public Page<ModelListResponse> modelList(ModelListRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<ModelConfigPO> byModelName = modelConfigRepository.findByModelName(request.getModelName(), pageable);
        /*Page<ModelListResponse> map = byModelName.map(ModelListResponse::from);*/
        return byModelName.map(modelConfigMapper::toListResponse);
    }

    @Override
    public ModelInfoResponse modelInfo(Integer id) {
        ModelConfigPO model = modelConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在: " + id));
        return modelConfigMapper.toResponse(model);
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断 authorization 是否为脱敏占位符（如 "sk-****"）。
     */
    private boolean isMasked(String authorization) {
        return authorization.contains("****") || authorization.contains("***");
    }
}
