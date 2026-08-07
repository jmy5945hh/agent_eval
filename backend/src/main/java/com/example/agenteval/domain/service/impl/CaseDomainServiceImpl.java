package com.example.agenteval.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.request.cases.CaseRequest;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import com.example.agenteval.domain.model.CaseAnswerListPO;
import com.example.agenteval.domain.model.EnumInfoPO;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.repository.CaseAnswerListPORespository;
import com.example.agenteval.domain.repository.EnumInfoPORespository;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import com.example.agenteval.domain.service.CaseDomainService;
import com.example.agenteval.domain.service.mapstruct.EvaluationCaseMapper;
import com.example.agenteval.domain.service.specification.EvaluationCasePOSpecs;
import com.example.agenteval.infrastructure.enums.DifficultyEnum;
import com.example.agenteval.infrastructure.enums.EnumTypeEnum;
import com.example.agenteval.infrastructure.enums.ImportanceEnum;
import com.example.agenteval.infrastructure.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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
    private final CaseAnswerListPORespository caseAnswerListPORespository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final EnumInfoPORespository enumInfoPORespository;
    private final MinioService minioService;
    private final EvaluationCaseMapper evaluationCaseMapper;

    // ==================== 案例 CRUD ====================

    /**
     * 新增案例。
     */
    @Override
    @Transactional
    public void createCase(CaseRequest request) {
        //校验
        baseCheck(request.getCategory(), request.getDifficulty(), request.getImportance());
        //上传
        String promptKey = minioService.createAndUploadFile(request.getPrompt());

        EvaluationCasePO entity = EvaluationCasePO.builder()
                .caseName(request.getCaseName())
                .promptKey(promptKey)
                .repo(request.getRepo())
                .branch(request.getBranch())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .importance(request.getImportance())
                .caseVersion(1)
                .remark(request.getRemark())
                .build();

        EvaluationCasePO saved = caseRepository.save(entity);
        if (CollUtil.isEmpty(request.getStandardAnswers())) {
            return;
        }
        insertCaseAnswer(request, saved);
        log.info("Case created: id={}, name={}, code=promptKey={}", saved.getId(), saved.getCaseName(),
                saved.getPromptKey());

    }

    /**
     * 编辑案例。
     * <p>若 Prompt 或标准答案发生变更，caseVersion 自动 +1。
     * 标准答案采用全量替换策略。</p>
     */
    @Override
    @Transactional
    public void updateCase(Integer caseId, CaseRequest request) {
        EvaluationCasePO entity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("案例不存在: " + caseId));
        baseCheck(request.getCategory(), request.getDifficulty(), request.getImportance());

        // 检测 prompt 是否变更
        boolean promptChanged = Objects.equals(request.getPrompt(), minioService.getAndReadFile(entity.getPromptKey()));
        //检查案例答案是否变更
        List<CaseAnswerListPO> byCaseId = caseAnswerListPORespository.findByCaseId(entity.getId());
        String dbAnswer = byCaseId.stream()
                .sorted(Comparator.comparing(CaseAnswerListPO::getStandardAnswerKey))
                .map(item -> item.getFilePath() + item.getStandardAnswerKey()).collect(Collectors.joining(""));
        String requestAnswer = request.getStandardAnswers().stream()
                .sorted(Comparator.comparing(CaseRequest.StandardAnswerItem::getFileKey))
                .map(item -> item.getPath() + item.getFileKey()).collect(Collectors.joining(""));
        boolean answerChanged = Objects.equals(dbAnswer, requestAnswer);
        if (!promptChanged || !answerChanged) {
            entity.setCaseVersion(entity.getCaseVersion() + 1);
        }
        String promptKey = entity.getPromptKey();
        if (!promptChanged) {
            promptKey = minioService.createAndUploadFile(request.getPrompt());
        }
        entity.setPromptKey(promptKey);
        entity.setCaseName(request.getCaseName());
        entity.setRepo(request.getRepo());
        entity.setBranch(request.getBranch());
        entity.setCategory(request.getCategory());
        entity.setDifficulty(request.getDifficulty());
        entity.setImportance(request.getImportance());
        entity.setRemark(request.getRemark());
        caseRepository.save(entity);
        //标准答案不一样，就重新录入
        if (!answerChanged) {
            caseAnswerListPORespository.deleteByCaseId(entity.getId());
            insertCaseAnswer(request, entity);
        }
        log.info("Case updated: id={}, name={}, version={}", caseId, entity.getCaseName(), entity.getCaseVersion());

    }

    private void insertCaseAnswer(CaseRequest request, EvaluationCasePO entity) {
        List<CaseAnswerListPO> caseAnswerListPOS = new ArrayList<>(request.getStandardAnswers().size());
        for (CaseRequest.StandardAnswerItem standardAnswer : request.getStandardAnswers()) {
            if (!minioService.checkFileExist(standardAnswer.getFileKey())) {
                throw new RuntimeException("文件" + standardAnswer.getFileKey() + "不存在");
            }
            caseAnswerListPOS.add(CaseAnswerListPO.builder()
                    .caseId(entity.getId())
                    .filePath(standardAnswer.getPath())
                    .standardAnswerKey(standardAnswer.getFileKey())
                    .build());
        }
        caseAnswerListPORespository.saveAll(caseAnswerListPOS);
    }

    private void baseCheck(Integer categoryId, Integer difficulty, Integer importance) {
        EnumInfoPO enumInfoPO = enumInfoPORespository.findAllByIdAndEnumType(categoryId, EnumTypeEnum.CaseType.getType());
        if (ObjUtil.isNull(enumInfoPO)) {
            throw new IllegalArgumentException("分类不存在:" + categoryId);
        }
        DifficultyEnum difficultyEnum = EnumUtil.findEnumByField(DifficultyEnum.class, DifficultyEnum.DIFFICULTY_NAME, difficulty);
        if (ObjUtil.isNull(difficultyEnum)) {
            throw new IllegalArgumentException("难度不存在:" + difficulty);
        }

        ImportanceEnum importanceEnum = EnumUtil.findEnumByField(ImportanceEnum.class, ImportanceEnum.IMPORTANCE_NAME, importance);
        if (ObjUtil.isNull(importanceEnum)) {
            throw new IllegalArgumentException("重要性不存在:" + importance);
        }
    }

    /**
     * 删除案例。
     * <p>先检查是否有测评任务引用该案例。有引用时抛出 {@link IllegalStateException}，
     * 由 Controller 层捕获后返回 409。无引用时直接物理删除。</p>
     */
    @Override
    @Transactional
    public void deleteCase(Integer caseId) {
        List<String> refs = getReferencedTaskIds(caseId);
        if (!refs.isEmpty()) {
            throw new IllegalStateException(
                    "该案例被 " + refs.size() + " 个测评任务引用，无法删除");
        }
        caseRepository.deleteById(caseId);
        caseAnswerListPORespository.deleteByCaseId(caseId);
        log.info("Case deleted: id={}", caseId);
    }

    @Override
    public Page<CaseListResponse> caseList(CaseListRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<EvaluationCasePO> evaluationCasePOS = caseRepository.findAll(EvaluationCasePOSpecs.caseListBuildSpec(request), pageable);
        List<EnumInfoPO> allByEnumType = enumInfoPORespository.findAllByEnumType(EnumTypeEnum.CaseType.getType());
        Map<Integer, EnumInfoPO> enumMap = allByEnumType.stream().collect(Collectors.toMap(EnumInfoPO::getId, Function.identity()));
        List<CaseListResponse> caseListResponses = evaluationCasePOS.getContent().stream().map(item -> evaluationCaseMapper.toCaseListResponse(item, enumMap)).collect(Collectors.toList());
        return new PageImpl<>(caseListResponses, evaluationCasePOS.getPageable(), evaluationCasePOS.getTotalElements());
    }

    // ==================== 关联查询 ====================

    /**
     * 查询案例被哪些测评任务引用。
     * <p>用于删除前的依赖检查。返回去重后的任务 ID 列表。</p>
     */
    public List<String> getReferencedTaskIds(Integer caseId) {
        List<TaskCaseRunPO> runs = caseRunRepository.findByCaseId(caseId);
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
            case "前端":
                return 1;
            case "Java后端":
                return 2;
            case "Python后端":
                return 3;
            case "AI智能体":
                return 4;
            case "安全测试":
                return 5;
            default:
                return 1;
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
