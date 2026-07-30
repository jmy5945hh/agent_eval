package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.CaseCreateRequest;
import com.example.agenteval.application.dto.CaseUpdateRequest;
import com.example.agenteval.application.dto.PageResponse;
import com.example.agenteval.application.dto.StandardAnswerUploadRequest;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.pojo.CaseFile;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.service.CaseDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.util.*;

/**
 * 案例管理控制器，负责案例的新增、编辑、删除、分页查询、关联任务检查及标准答案上传。
 * 只读查询（列表、详情、prompt/标准答案按需加载）由 ReferenceDataController 提供。
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseDomainService caseDomainService;
    private final EvaluationCasePORespository caseRepository;

    // ==================== 分页查询（增强） ====================

    /**
     * 分页查询案例列表，支持 category、difficulty、keyword 筛选和 sortBy 排序。
     */
    @GetMapping("/paged")
    public CommonResponse<PageResponse<EvaluationCasePO>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        Specification<EvaluationCasePO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                predicates.add(cb.equal(root.get("difficulty"), mapDifficulty(difficulty)));
            }
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("repo")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("name".equals(sortBy)) sort = Sort.by(Sort.Direction.ASC, "name");
        if ("difficulty".equals(sortBy)) sort = Sort.by(Sort.Direction.ASC, "difficulty");

        Page<EvaluationCasePO> result = caseRepository.findAll(spec, PageRequest.of(page, size, sort));
        return CommonResponse.success(PageResponse.of(
                result.getContent(), result.getTotalElements(), page + 1, size));
    }

    // ==================== 案例 CRUD ====================

    /**
     * 新增案例。自动生成案例编号，默认 version=1, difficulty=中。
     */
    @PostMapping
    public ResponseEntity<CommonResponse<EvaluationCasePO>> createCase(
            @Valid @RequestBody CaseCreateRequest request) {
        log.info("Creating case: name={}, category={}", request.getName(), request.getCategory());
        EvaluationCasePO created = caseDomainService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(created));
    }

    /**
     * 编辑案例。Prompt 或标准答案变更时 caseVersion 自动 +1。
     */
    @PutMapping("/{caseId}")
    public CommonResponse<EvaluationCasePO> updateCase(
            @PathVariable Long caseId,
            @Valid @RequestBody CaseUpdateRequest request) {
        log.info("Updating case: id={}, name={}, category={}", caseId, request.getName(), request.getCategory());
        EvaluationCasePO updated = caseDomainService.updateCase(caseId, request);
        return CommonResponse.success(updated);
    }

    /**
     * 删除案例。有关联任务时若未传 force=true 则返回 409。
     */
    @DeleteMapping("/{caseId}")
    public ResponseEntity<CommonResponse<Void>> deleteCase(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "false") boolean force) {
        List<String> refs = caseDomainService.getReferencedTaskIds(caseId);
        if (!refs.isEmpty() && !force) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("referencedTasks", refs);
            body.put("message", "该案例被 " + refs.size() + " 个测评任务引用，确认删除？");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409)
                            .message("案例被 " + refs.size() + " 个任务引用").build());
        }
        log.info("Deleting case: id={}, force={}", caseId, force);
        caseDomainService.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }

    // ==================== 关联任务查询 ====================

    /**
     * 查询案例被哪些测评任务引用，用于删除前依赖检查。
     */
    @GetMapping("/{caseId}/tasks")
    public CommonResponse<Map<String, Object>> getCaseTasks(@PathVariable Long caseId) {
        List<String> refs = caseDomainService.getReferencedTaskIds(caseId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("caseId", caseId);
        result.put("count", refs.size());
        result.put("taskIds", refs);
        return CommonResponse.success(result);
    }

    // ==================== 标准答案管理 ====================

    /**
     * 上传/替换案例的标准答案文件。
     */
    @PostMapping("/{caseId}/standard-answers")
    public CommonResponse<List<CaseFile>> uploadStandardAnswers(
            @PathVariable Long caseId,
            @Valid @RequestBody StandardAnswerUploadRequest request) {
        log.info("Uploading {} standard answer files for case {}", request.getFiles().size(), caseId);
        List<CaseFile> saved = caseDomainService.saveStandardAnswers(caseId, request.getFiles());
        return CommonResponse.success(saved);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将中文难度映射为数据库存储的整数值。
     * 1=高, 2=中, 3=低
     */
    private Integer mapDifficulty(String difficulty) {
        if ("高".equals(difficulty)) return 1;
        if ("低".equals(difficulty)) return 3;
        return 2; // 默认中
    }
}
