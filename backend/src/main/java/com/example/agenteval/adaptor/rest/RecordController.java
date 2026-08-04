package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.record.RecordListResponse;
import com.example.agenteval.application.dto.response.record.SummaryDataResponse;
import com.example.agenteval.domain.service.ExportService;
import com.example.agenteval.domain.service.RecordQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
     * 汇总数据
     *
     * @return
     */
    @GetMapping("/summary/data")
    public ResponseEntity<CommonResponse<SummaryDataResponse>> summaryData() {
        return ResponseEntity.ok(CommonResponse.success(recordQueryService.summaryData()));
    }


    /**
     * 列表查询
     *
     * @param request
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<CommonResponse<Page<RecordListResponse>>> recordList(@Valid @RequestBody RecordListRequest request) {
        return ResponseEntity.ok(CommonResponse.success(recordQueryService.recordList(request)));
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
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
        response.flushBuffer();
    }
}
