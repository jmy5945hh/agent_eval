package com.example.agenteval.adaptor.rest;

import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.ErrorInfo;
import com.example.agenteval.domain.model.pojo.TrajectoryEntry;
import com.example.agenteval.domain.service.ExecutionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行详情控制器，负责执行记录详情和轨迹数据的查询。
 * 数据来源为 TaskCaseRun（JPA 实体）和对象存储（轨迹）。
 */
@Slf4j
@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionDomainService executionDomainService;

    /**
     * 查询单条执行记录详情，含状态、执行统计和错误信息。
     */
    @GetMapping("/runs/{runId}")
    public CommonResponse<Map<String, Object>> getRunDetail(@PathVariable Long runId) {
        TaskCaseRunPO run = executionDomainService.getRunDetail(runId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("runId", run.getId());
        result.put("caseId", run.getCaseId());
        result.put("taskId", run.getTaskId());
        result.put("status", run.getStatus());
        result.put("stats", buildStats(run));
        result.put("error", buildError(run));
        return CommonResponse.success(result);
    }

    /**
     * 查询单条执行记录的完整轨迹，按 seq 升序排列。
     */
    @GetMapping("/runs/{runId}/trajectory")
    public CommonResponse<List<TrajectoryEntry>> getTrajectory(@PathVariable Long runId) {
        List<TrajectoryEntry> trajectory = executionDomainService.getTrajectory(runId);
        return CommonResponse.success(trajectory);
    }

    private Map<String, Object> buildStats(TaskCaseRunPO run) {
        Map<String, Object> stats = new LinkedHashMap<>();
//        stats.put("rounds", run.getRounds() != null ? run.getRounds() : 0);
//        stats.put("tokensIn", run.getTokensIn() != null ? run.getTokensIn() : 0);
//        stats.put("tokensOut", run.getTokensOut() != null ? run.getTokensOut() : 0);
//        stats.put("durationMs", run.getDurationMs() != null ? run.getDurationMs() : 0);
//        stats.put("attempts", run.getAttempts() != null ? run.getAttempts() : 0);
        return stats;
    }

    private ErrorInfo buildError(TaskCaseRunPO run) {
        if (run.getErrorInfoKey() == null) return null;
        return new ErrorInfo("agent", "");
    }
}
