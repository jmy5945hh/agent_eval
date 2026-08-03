package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.response.PageResponse;
import com.example.agenteval.application.dto.TaskResponse;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.domain.service.ExportService;
import com.example.agenteval.domain.service.RecordQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 测评记录控制器，负责历史记录的列表查询（分页+筛选）、详情查询和评分明细导出。
 */
@Slf4j
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordQueryService recordQueryService;
    private final ExportService exportService;

    /**
     * 分页查询历史测评记录，支持 agentId、modelId、status 和创建时间范围筛选。
     */
    @GetMapping
    public CommonResponse<PageResponse<TaskResponse>> listRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String modelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        log.info("Listing records: page={}, size={}, agentId={}, modelId={}, status={}",
                page, size, agentId, modelId, status);

        PageResponse<TaskResponse> result = recordQueryService.listRecords(
                page, size, agentId, modelId, status, dateFrom, dateTo);
        return CommonResponse.success(result);
    }

    /**
     * 查询测评记录完整详情，聚合任务信息、执行统计和评分汇总。
     */
    @GetMapping("/{taskId}")
    public CommonResponse<TaskResponse> getRecordDetail(@PathVariable Long taskId) {
        log.info("Getting record detail: taskId={}", taskId);
        TaskResponse detail = recordQueryService.getRecordDetail(taskId);
        return CommonResponse.success(detail);
    }

    /**
     * 导出指定测评任务的评分明细为 Excel 文件。
     */
    @GetMapping("/{taskId}/export")
    public void exportScores(@PathVariable Long taskId,
                             HttpServletResponse response) throws IOException {
        log.info("Exporting scores for record {}", taskId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String filename = exportService.exportScores(taskId, response.getOutputStream());
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
        response.flushBuffer();
    }
}
