package com.example.agenteval.domain.service.impl;

import com.example.agenteval.application.dto.request.cases.CaseCreateRequest;
import com.example.agenteval.application.dto.request.cases.CaseUpdateRequest;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.CaseFile;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import com.example.agenteval.domain.service.CaseDomainService;
import com.example.agenteval.infrastructure.storage.CaseContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 案例领域服务实现 — {@link CaseDomainService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>案例 CRUD：自动生成编号、版本自增、关联检查。</li>
 *   <li>标准答案管理：将 prompt 和标准答案文件持久化到对象存储。</li>
 *   <li>删除依赖检查：删除前检查案例是否被测评任务引用。</li>
 * </ul>
 *
 * @see CaseDomainService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseDomainServiceImpl implements CaseDomainService {

    private final EvaluationCasePORespository caseRepository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final CaseContentService caseContentService;

    // ==================== 案例 CRUD ====================

    /**
     * 新增案例。
     */
    @Override
    @Transactional
    public EvaluationCasePO createCase(CaseCreateRequest request) {
        EvaluationCasePO entity = EvaluationCasePO.builder()
                .caseName(request.getCaseName())
                .promptKey("")
                .repo(request.getRepo())
                .branch(request.getBranch())
                .category(mapCategory(request.getCategory()))
                .difficulty(mapDifficulty(request.getDifficulty()))
                .importance(mapImportance(request.getImportance()))
                .caseVersion(1)
                .remark(request.getRemark() != null ? request.getRemark() : "")
                .build();

        EvaluationCasePO saved = caseRepository.save(entity);

        // 保存 prompt 到对象存储
        try {
            caseContentService.savePrompt(saved, request.getPrompt());
        } catch (Exception e) {
            log.warn("Failed to save prompt to object storage for case {}", saved.getId(), e);
        }

        // 保存标准答案到对象存储
        if (request.getStandardAnswers() != null && !request.getStandardAnswers().isEmpty()) {
            List<CaseFile> files = request.getStandardAnswers().stream()
                    .map(item -> new CaseFile(item.getPath(), item.getFile()))
                    .collect(Collectors.toList());
            try {
                caseContentService.saveStandardAnswer(saved, files);
            } catch (Exception e) {
                log.warn("Failed to save standard answers for case {}", saved.getId(), e);
            }
        }

        caseRepository.save(saved);
        log.info("Case created: id={}, name={}, code=promptKey={}", saved.getId(), saved.getCaseName(),
                saved.getPromptKey());
        return saved;
    }

    /**
     * 编辑案例。
     * <p>若 Prompt 或标准答案发生变更，caseVersion 自动 +1。
     * 标准答案采用全量替换策略。</p>
     */
    @Override
    @Transactional
    public EvaluationCasePO updateCase(CaseUpdateRequest request) {
        EvaluationCasePO entity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new IllegalArgumentException("案例不存在: " + request.getCaseId()));

        // 检测 prompt 是否变更
        boolean promptChanged = !Objects.equals(request.getPrompt(),
                caseContentService.loadPrompt(entity));

        // 版本号自增
        if (promptChanged || (request.getStandardAnswers() != null && !request.getStandardAnswers().isEmpty())) {
            int newVersion = entity.getCaseVersion() + 1;
            entity.setCaseVersion(newVersion);
        }

        entity.setCaseName(request.getCaseName());
        entity.setRepo(request.getRepo());
        entity.setBranch(request.getBranch());
        entity.setCategory(mapCategory(request.getCategory()));
        entity.setDifficulty(mapDifficulty(request.getDifficulty()));
        entity.setImportance(mapImportance(request.getImportance()));
        entity.setRemark(request.getRemark() != null ? request.getRemark() : "");

        caseRepository.save(entity);

        // 更新 prompt 到对象存储
        try {
            caseContentService.savePrompt(entity, request.getPrompt());
        } catch (Exception e) {
            log.warn("Failed to save prompt for case {}", request.getCaseId(), e);
        }

        // 全量替换标准答案
        if (request.getStandardAnswers() != null && !request.getStandardAnswers().isEmpty()) {
            List<CaseFile> files = request.getStandardAnswers().stream()
                    .map(item -> new CaseFile(item.getPath(), item.getFile()))
                    .collect(Collectors.toList());
            try {
                caseContentService.saveStandardAnswer(entity, files);
            } catch (Exception e) {
                log.warn("Failed to save standard answers for case {}", request.getCaseId(), e);
            }
        }

        caseRepository.save(entity);
        log.info("Case updated: id={}, name={}, version={}", request.getCaseId(), entity.getCaseName(), entity.getCaseVersion());
        return entity;
    }

    /**
     * 删除案例。
     * <p>先检查是否有测评任务引用该案例。有引用时抛出 {@link IllegalStateException}，
     * 由 Controller 层捕获后返回 409。无引用时直接物理删除。</p>
     */
    @Override
    @Transactional
    public void deleteCase(Long caseId) {
        List<String> refs = getReferencedTaskIds(caseId);
        if (!refs.isEmpty()) {
            throw new IllegalStateException(
                    "该案例被 " + refs.size() + " 个测评任务引用，无法删除");
        }
        caseRepository.deleteById(caseId.intValue());
        log.info("Case deleted: id={}", caseId);
    }

    // ==================== 关联查询 ====================

    /**
     * 查询案例被哪些测评任务引用。
     * <p>用于删除前的依赖检查。返回去重后的任务 ID 列表。</p>
     */
    @Override
    public List<String> getReferencedTaskIds(Long caseId) {
        List<TaskCaseRunPO> runs = caseRunRepository.findByCaseId(caseId.intValue());
        return runs.stream()
                .map(run -> String.valueOf(run.getTaskId()))
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== 辅助方法 ====================

    /**
     * 将中文分类名称映射为数据库存储的 int 值。
     * <ul>
     *   <li>前端 → 1</li>
     *   <li>Java后端 → 2</li>
     *   <li>Python后端 → 3</li>
     *   <li>AI智能体 → 4</li>
     *   <li>安全测试 → 5</li>
     *   <li>默认（含 null）→ 1（前端）</li>
     * </ul>
     */
    private int mapCategory(String category) {
        if (category == null) return 1;
        switch (category) {
            case "前端":     return 1;
            case "Java后端":  return 2;
            case "Python后端": return 3;
            case "AI智能体":  return 4;
            case "安全测试":  return 5;
            default:        return 1;
        }
    }

    /**
     * 将中文难度映射为 int 值。
     * <ul>
     *   <li>高 → 1</li>
     *   <li>中 / null → 2</li>
     *   <li>低 → 3</li>
     * </ul>
     */
    private int mapDifficulty(String difficulty) {
        if ("高".equals(difficulty)) return 1;
        if ("低".equals(difficulty)) return 3;
        return 2; // 默认中
    }

    /**
     * 将中文重要性映射为 int 值。
     * <ul>
     *   <li>高 → 1</li>
     *   <li>中 / null → 2</li>
     *   <li>低 → 3</li>
     * </ul>
     */
    private int mapImportance(String importance) {
        if ("高".equals(importance)) return 1;
        if ("低".equals(importance)) return 3;
        return 2; // 默认中
    }
}
