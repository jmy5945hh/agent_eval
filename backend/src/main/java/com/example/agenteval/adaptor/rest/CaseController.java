package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.cases.CaseCreateRequest;
import com.example.agenteval.application.dto.request.cases.CaseUpdateRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.service.CaseDomainService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 案例管理控制器，负责案例的新增、编辑、删除、分页查询、关联任务检查及标准答案上传。
 * 只读查询（列表、详情、prompt/标准答案按需加载）由 ReferenceDataController 提供。
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Api(tags = "案例管理控制器")
@ApiIgnore
public class CaseController {

    private final CaseDomainService caseDomainService;
    private final EvaluationCasePORespository caseRepository;

    // ==================== 分页查询（增强） ====================

    /**
     * 分页查询案例列表，支持 category、difficulty、keyword 筛选和 sortBy 排序。
     */
    @GetMapping("/paged")
    public CommonResponse<Page<EvaluationCasePO>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortBy) {
        boolean hasFilter = (category != null && !category.isEmpty())
                || (difficulty != null && !difficulty.isEmpty())
                || (keyword != null && !keyword.isEmpty());

        Page<EvaluationCasePO> result;
        if (hasFilter) {
            Specification<EvaluationCasePO> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (category != null && !category.isEmpty()) {
                    predicates.add(cb.equal(root.get("category"), mapCategoryInt(category)));
                }
                if (difficulty != null && !difficulty.isEmpty()) {
                    predicates.add(cb.equal(root.get("difficulty"), mapDifficulty(difficulty)));
                }
                if (keyword != null && !keyword.isEmpty()) {
                    String pattern = "%" + keyword.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("caseName")), pattern),
                            cb.like(cb.lower(root.get("repo")), pattern)
                    ));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            result = caseRepository.findAll(spec, buildPageRequest(page, size, sortBy));
        } else {
            result = caseRepository.findAll(buildPageRequest(page, size, sortBy));
        }

        return CommonResponse.success(result);
    }

    private PageRequest buildPageRequest(int page, int size, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "caseName":
                sort = Sort.by(Sort.Direction.ASC, "caseName");
                break;
            case "difficulty":
                sort = Sort.by(Sort.Direction.ASC, "difficulty");
                break;
            case "createTime":
                sort = Sort.by(Sort.Direction.DESC, "createTime");
                break;
            default:
                sort = Sort.by(Sort.Direction.DESC, "id");
                break;
        }
        return PageRequest.of(page, size, sort);
    }

    // ==================== 案例 CRUD ====================

    /**
     * 新增案例（支持在创建时直接上传标准答案文件）。自动生成案例编号，默认 version=1。
     * 使用 multipart/form-data 格式，标准答案文件通过 standardAnswers[0].file 等字段上传。
     */
    @PostMapping(path = "/createCase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<EvaluationCasePO> createCase(@Valid @ModelAttribute CaseCreateRequest request) {
        log.info("Creating case: name={}, category={}", request.getCaseName(), request.getCategory());
        EvaluationCasePO created = caseDomainService.createCase(request);
        return CommonResponse.success(created);
    }

    /**
     * 编辑案例。Prompt 或标准答案变更时 caseVersion 自动 +1。
     * 使用 multipart/form-data 格式，标准答案文件通过 standardAnswers[0].content 等字段上传。
     */
    @PostMapping(path = "/updateCase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<EvaluationCasePO> updateCase(@Valid @ModelAttribute CaseUpdateRequest request) {
        log.info("Updating case: id={}, name={}, category={}", request.getCaseId(), request.getCaseName(), request.getCategory());
        EvaluationCasePO updated = caseDomainService.updateCase(request);
        return CommonResponse.success(updated);
    }

    /**
     * 删除案例。有关联任务时若未传 force=true 则返回 409。
     */
    @DeleteMapping("/{caseId}")
    public CommonResponse<Void> deleteCase(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "false") boolean force) {
        List<String> refs = caseDomainService.getReferencedTaskIds(caseId);
        if (!refs.isEmpty() && !force) {
            return CommonResponse.<Void>builder()
                    .code(409)
                    .message("案例被 " + refs.size() + " 个任务引用，无法删除")
                    .data(null)
                    .build();
        }
        log.info("Deleting case: id={}, force={}", caseId, force);
        caseDomainService.deleteCase(caseId);
        return CommonResponse.success(null);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将中文分类映射为数据库存储的整数值。
     * 1=前端, 2=Java后端, 3=Python后端, 4=AI智能体, 5=安全测试
     */
    private Integer mapCategoryInt(String category) {
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
     * 将中文难度映射为数据库存储的整数值。
     * 1=高, 2=中, 3=低
     */
    private Integer mapDifficulty(String difficulty) {
        if ("高".equals(difficulty)) return 1;
        if ("低".equals(difficulty)) return 3;
        return 2; // 默认中
    }
}
