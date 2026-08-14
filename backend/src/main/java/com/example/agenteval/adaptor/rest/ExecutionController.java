package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.application.dto.request.record.TaskCaseListRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.record.*;
import com.example.agenteval.domain.model.pojo.ScoreCommentResult;
import com.example.agenteval.domain.service.ExecutionService;
import com.example.agenteval.domain.service.ExportService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 测评记录控制器，负责历史记录的列表查询（分页+筛选）、详情查询和评分明细导出。
 */
@Slf4j
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Api(tags = "执行中心控制器")
@ApiSupport(order = 7)
public class ExecutionController {

    private final ExecutionService executionService;
    private final ExportService exportService;

    /**
     * 汇总数据
     *
     * @return
     */
    @ApiOperation(value = "查询汇总数据")
    @GetMapping("/summary/data")
    public ResponseEntity<CommonResponse<SummaryDataResponse>> summaryData() {
        return ResponseEntity.ok(CommonResponse.success(executionService.summaryData()));
    }


    /**
     * 列表查询
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "查询评测列表")
    @PostMapping("/list")
    public ResponseEntity<CommonResponse<Page<RecordListResponse>>> recordList(@Valid @RequestBody RecordListRequest request) {
        return ResponseEntity.ok(CommonResponse.success(executionService.recordList(request)));
    }

    /**
     * 导出评测记录
     *
     * @param response
     */
    @ApiOperation(value = "导出评测记录")
    @PostMapping("/export")
    public void exportRecord(HttpServletResponse response, @Valid @RequestBody RecordListRequest request) {
        executionService.exportRecord(response, request);
    }

    @ApiOperation(value = "查询评测任务详情")
    @GetMapping("/detail/{taskId}")
    public ResponseEntity<CommonResponse<TaskDetailResponse>> taskDetail(@ApiParam(value = "任务id", required = true) @PathVariable Integer taskId) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskDetail(taskId)));
    }

    @ApiOperation(value = "查询评测任务案例列表")
    @PostMapping("/detail/task/{taskId}/case/list")
    public ResponseEntity<CommonResponse<Page<TaskCaseListResponse>>> taskCaseList(@ApiParam(value = "任务id", required = true) @PathVariable Integer taskId,
                                                                                   @Valid @RequestBody TaskCaseListRequest taskCaseListRequest) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskCaseList(taskId, taskCaseListRequest)));
    }

    @ApiOperation(value = "查询评测任务案例详情")
    @GetMapping("/detail/task/case/{runCaseId}")
    public ResponseEntity<CommonResponse<TaskCaseInfoResponse>> taskCaseInfo(@ApiParam(value = "任务案例id", required = true) @PathVariable Integer runCaseId) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskCaseInfo(runCaseId)));
    }

    @ApiOperation(value = "查询评测任务案例执行轨迹")
    @GetMapping("/detail/task/case/trace/{runCaseId}")
    public ResponseEntity<CommonResponse<String>> taskCaseExecutionTrace(@ApiParam(value = "任务案例id", required = true) @PathVariable Integer runCaseId) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskCaseExecutionTrace(runCaseId)));
    }

    @ApiOperation(value = "查询评测任务案例提示信息")
    @GetMapping("/detail/task/case/prompt/{runCaseId}")
    public ResponseEntity<CommonResponse<String>> taskCasePrompt(@ApiParam(value = "任务案例id", required = true) @PathVariable Integer runCaseId) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskCasePrompt(runCaseId)));
    }

    @ApiOperation(value = "查询评测任务案例错误信息")
    @GetMapping("/detail/task/case/error/{runCaseId}")
    public ResponseEntity<CommonResponse<String>> taskCaseError(@ApiParam(value = "任务案例id", required = true) @PathVariable Integer runCaseId) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskCaseError(runCaseId)));
    }

    @ApiOperation(value = "查询评测任务案例评分信息")
    @GetMapping("/detail/task/case/score/{runCaseId}")
    public ResponseEntity<CommonResponse<List<ScoreCommentResult>>> taskCaseScoreComment(@ApiParam(value = "任务案例id", required = true) @PathVariable Integer runCaseId) {
        return ResponseEntity.ok(CommonResponse.success(executionService.taskCaseScoreComment(runCaseId)));
    }

}
