package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.ScoringStandardRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.domain.service.ScoringStandardDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 评分标准管理控制器，负责评分标准的版本管理（新增/编辑/删除）。
 * 只读列表查询由 ReferenceDataController 提供。
 *
 * <p>版本号唯一，同一时间只有一个当前版本。所有维度权重之和必须等于 100%。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/scoring-standards")
@RequiredArgsConstructor
public class ScoringStandardController {

    private final ScoringStandardDomainService scoringStandardDomainService;

    /**
     * 新增评分标准版本。后端校验权重合计为 100%，不通过返回 400。
     */
    @PostMapping
    public ResponseEntity<CommonResponse<Void>> createStandard(
            @Valid @RequestBody ScoringStandardRequest request) {
        log.info("Creating scoring standard: version={}, dimensions={}",
                request.getVersion(), request.getDimensions().size());
        scoringStandardDomainService.createStandard(request);
        return ResponseEntity.ok(CommonResponse.success());
    }

    /**
     * 编辑评分标准版本。版本号不可修改，仅允许修改 note 和 dimensions。
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> updateStandard(
            @PathVariable Integer id,
            @Valid @RequestBody ScoringStandardRequest request) {
        log.info("Updating scoring standard: id={}, version={}", id, request.getVersion());
        scoringStandardDomainService.updateStandard(id, request);
        return ResponseEntity.ok(CommonResponse.success());
    }

    /**
     * 删除评分标准版本。被测评任务引用时返回 409。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteStandard(@PathVariable Integer id) {
        log.info("Deleting scoring standard: id={}", id);
        try {
            scoringStandardDomainService.deleteStandard(id);
            return ResponseEntity.ok(CommonResponse.success());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.<Void>builder().code(409).message(e.getMessage()).build());
        }
    }

    /**
     * 校验评分维度权重合计是否为 100%
     */
    private void validateWeights(ScoringStandardRequest request) {
        int total = request.getDimensions().stream()
                .mapToInt(d -> d.getWeight() != null ? d.getWeight() : 0)
                .sum();
        if (total != 100) {
            throw new IllegalArgumentException(
                    "评分维度权重合计必须为 100%，当前为 " + total + "%");
        }
    }
}
