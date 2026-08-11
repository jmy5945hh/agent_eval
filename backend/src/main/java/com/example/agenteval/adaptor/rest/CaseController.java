package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.request.cases.CaseRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.service.EvaluationCaseService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 案例管理控制器，负责案例的新增、编辑、删除、分页查询、关联任务检查及标准答案上传。
 * 只读查询（列表、详情、prompt/标准答案按需加载）由 ReferenceDataController 提供。
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Api(tags = "案例管理控制器")
@ApiSupport(order = 5)
public class CaseController {

    private final EvaluationCaseService evaluationCaseService;
    private final EvaluationCasePORespository caseRepository;


    /**
     * 新增案例（支持在创建时直接上传标准答案文件）。自动生成案例编号，默认 version=1。
     * 使用 multipart/form-data 格式，标准答案文件通过 standardAnswers[0].file 等字段上传。
     */
    @ApiOperation(value = "新增案例")
    @PostMapping(path = "/create/case")
    public ResponseEntity<CommonResponse<Void>> createCase(@Valid @RequestBody CaseRequest request) {
        log.info("Creating case: name={}, category={}", request.getCaseName(), request.getCategory());
        evaluationCaseService.createCase(request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 编辑案例。Prompt 或标准答案变更时 caseVersion 自动 +1。
     * 使用 multipart/form-data 格式，标准答案文件通过 standardAnswers[0].content 等字段上传。
     */
    @ApiOperation(value = "编辑案例")
    @PostMapping(path = "/update/case/{caseId}")
    public ResponseEntity<CommonResponse<Void>> updateCase(@ApiParam(value = "案例id", required = true) @PathVariable Integer caseId, @Valid @RequestBody CaseRequest request) {
        log.info("Updating case: id={}, name={}, category={}", caseId, request.getCaseName(), request.getCategory());
        evaluationCaseService.updateCase(caseId, request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    /**
     * 删除案例。有关联任务时若未传 force=true 则返回 409。
     */
    @ApiOperation(value = "删除案例")
    @DeleteMapping("/{caseId}")
    public ResponseEntity<CommonResponse<Void>> deleteCase(@ApiParam(value = "案例id", required = true) @PathVariable Integer caseId) {
        log.info("Deleting case: id={}", caseId);
        evaluationCaseService.deleteCase(caseId);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @ApiOperation(value = "案例列表")
    @PostMapping(path = "/list")
    public ResponseEntity<CommonResponse<Page<CaseListResponse>>> caseList(@Valid @RequestBody CaseListRequest request) {
        return ResponseEntity.ok(CommonResponse.success(evaluationCaseService.caseList(request)));
    }
}
