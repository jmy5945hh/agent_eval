package com.example.agenteval.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.agenteval.application.dto.BasePageRequest;
import com.example.agenteval.application.dto.request.score.ScoringStandardRequest;
import com.example.agenteval.application.dto.response.score.ScoringStandardListResponse;
import com.example.agenteval.domain.model.ScoringStandardPO;
import com.example.agenteval.domain.model.pojo.ScoringDimension;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.ScoringStandardPORespository;
import com.example.agenteval.domain.service.ScoringStandardDomainService;
import com.example.agenteval.infrastructure.util.MapUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 评分标准领域服务实现 — {@link ScoringStandardDomainService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>评分标准版本 CRUD：新增、编辑、删除评分标准版本。</li>
 *   <li>当前版本独占：设为当前版本时自动取消其他版本的当前标记。</li>
 *   <li>权重校验：所有维度权重之和必须为 100%。</li>
 *   <li>删除依赖检查：删除前校验是否被测评任务引用。</li>
 * </ul>
 *
 * <h4>维度存储说明</h4>
 * <p>评分维度 {@link ScoringDimension} 列表序列化为 JSON 字符串，
 *
 * @see ScoringStandardDomainService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringStandardDomainServiceImpl implements ScoringStandardDomainService {

    private final ScoringStandardPORespository standardRepository;
    private final EvaluationTaskPORespository evaluationTaskRepository;
    private final ObjectMapper objectMapper;

    // ==================== 评分标准 CRUD ====================

    /**
     * 新增评分标准版本。
     * <p>版本号全局唯一；所有维度权重之和必须等于 100%；
     * 若 isCurrent=true，先将其他版本的 isCurrent 置为 0。</p>
     */
    @Override
    @Transactional
    public void createStandard(ScoringStandardRequest request) {
        // 版本号唯一性校验
        if (standardRepository.existsByVersion(request.getVersion())) {
            throw new IllegalArgumentException("版本号已存在: " + request.getVersion());
        }

        // 维度权重校验
        validateWeightSum(request.getDimensions());

        // 若设为当前版本，取消其他版本的当前标记
        boolean current = request.getCurrentVersion() != null && request.getCurrentVersion();
        if (current) {
            clearCurrentVersions();
        }

        ScoringStandardPO entity = ScoringStandardPO.builder()
                .version(request.getVersion())
                .isCurrent(MapUtil.mapBoolean(current, false))
                .note(request.getNote())
                .dimensions(serializeDimensions(request.getDimensions()))
                .build();

        ScoringStandardPO saved = standardRepository.save(entity);
        log.info("Scoring standard created: id={}, version={}, isCurrent={}, dimensions={}",
                saved.getId(), saved.getVersion(), current, request.getDimensions().size());
    }

    /**
     * 编辑评分标准版本。
     * <p>版本号不可修改（接口契约）；仅允许修改 note 和 dimensions。
     * 权重之和校验同样适用。</p>
     */
    @Override
    @Transactional
    public void updateStandard(Integer id, ScoringStandardRequest request) {
        ScoringStandardPO entity = standardRepository.findById(id.intValue())
                .orElseThrow(() -> new IllegalArgumentException("评分标准不存在: " + id));

        // 权重校验
        validateWeightSum(request.getDimensions());

        // 若请求中 isCurrent=true，取消其他版本的当前标记
        if (request.getCurrentVersion() != null) {
            boolean current = request.getCurrentVersion();
            if (current) {
                clearCurrentVersions();
            }
            entity.setIsCurrent(current ? (byte) 1 : (byte) 0);
        }

        // 仅更新 note 和 dimensions（版本号不可修改）
        if (StrUtil.isNotBlank(request.getNote())) {
            entity.setNote(request.getNote());
        }
        if (CollUtil.isNotEmpty(request.getDimensions())) {
            entity.setDimensions(serializeDimensions(request.getDimensions()));
        }

        ScoringStandardPO saved = standardRepository.save(entity);
        log.info("Scoring standard updated: id={}, version={}",
                id, saved.getVersion());
    }

    /**
     * 删除评分标准版本。
     * <p>检查是否被测评任务引用（通过 scoreStandardId），有引用则抛出
     * {@link IllegalStateException}，由 Controller 返回 409。</p>
     */
    @Override
    @Transactional
    public void deleteStandard(Integer id) {
        int standardId = id.intValue();

        // 检查是否存在
        if (!standardRepository.existsById(standardId)) {
            throw new IllegalArgumentException("评分标准不存在: " + id);
        }

        // 检查是否被测评任务引用
        if (evaluationTaskRepository.existsByScoreStandardId(standardId)) {
            throw new IllegalStateException("该评分标准已被测评任务引用，无法删除");
        }

        standardRepository.deleteById(standardId);
        log.info("Scoring standard deleted: id={}", id);
    }

    @Override
    public Page<ScoringStandardListResponse> scoringStandardList(BasePageRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<ScoringStandardPO> scoringStandardPOS = standardRepository.findAll(pageable);
        List<ScoringStandardPO> content = scoringStandardPOS.getContent();
        List<ScoringStandardListResponse> returnList = new ArrayList<>();
        content.forEach(item -> {
            List<ScoringDimension> scoringDimension = JSONUtil.toBean(item.getDimensions(), new TypeReference<List<ScoringDimension>>() {
            }, true);
            returnList.add(ScoringStandardListResponse.builder()
                    .id(item.getId())
                    .version(item.getVersion())
                    .note(item.getNote())
                    .currentVersion(item.getIsCurrent() == 1)
                    .dimensions(scoringDimension).build());
        });
        return new PageImpl<>(returnList, scoringStandardPOS.getPageable(), scoringStandardPOS.getTotalElements());
    }

    // ==================== 辅助方法 ====================

    /**
     * 校验所有评分维度权重之和是否等于 100%。
     *
     * @throws IllegalArgumentException 权重之和不等于 100 时抛出
     */
    private void validateWeightSum(List<ScoringDimension> dimensions) {
        if (CollUtil.isEmpty(dimensions)) {
            return;
        }
        int total = dimensions.stream()
                .mapToInt(d -> d.getWeight() != null ? d.getWeight() : 0)
                .sum();
        if (total != 100) {
            throw new IllegalArgumentException(
                    "评分维度权重合计必须为 100%，当前为 " + total + "%");
        }
    }

    /**
     * 将所有版本的 isCurrent 标记清除（设为 0）。
     */
    private void clearCurrentVersions() {
        List<ScoringStandardPO> currentVersions = standardRepository.findByIsCurrent((byte) 1);
        if (!currentVersions.isEmpty()) {
            for (ScoringStandardPO standard : currentVersions) {
                standard.setIsCurrent((byte) 0);
            }
            standardRepository.saveAll(currentVersions);
            log.debug("Cleared isCurrent flag for {} versions", currentVersions.size());
        }
    }

    /**
     * 将评分维度列表序列化为 JSON 字符串。
     */
    private String serializeDimensions(List<ScoringDimension> dimensions) {
        try {
            return objectMapper.writeValueAsString(dimensions);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("维度序列化失败", e);
        }
    }
}
