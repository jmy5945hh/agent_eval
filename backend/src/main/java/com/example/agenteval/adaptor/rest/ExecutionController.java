package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.record.RecordListResponse;
import com.example.agenteval.application.dto.response.record.SummaryDataResponse;
import com.example.agenteval.domain.service.ExecutionService;
import com.example.agenteval.domain.service.ExportService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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

}
