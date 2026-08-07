package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.AddCasesRequest;
import com.example.agenteval.application.dto.ScoreRequest;
import com.example.agenteval.application.dto.request.task.TaskCreateRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.task.TaskResponse;
import com.example.agenteval.domain.model.pojo.CaseRun;
import com.example.agenteval.domain.service.TaskDomainService;
import com.example.agenteval.infrastructure.executor.AgentExecutorService;
import com.example.agenteval.infrastructure.scoring.ScoringClientService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测评任务控制器，负责任务的创建、查询、执行控制和评分触发。
 * 所有业务逻辑统一委托给 {@link TaskDomainService}。
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Api(tags = "评测任务控制器")
@ApiIgnore
@ApiSupport(order = 6)
public class TaskController {

    private final TaskDomainService taskDomainService;
    private final AgentExecutorService agentExecutorService;
    private final ScoringClientService scoringClientService;

    /**
     * 获取所有测评任务列表。
     */
    @GetMapping
    public CommonResponse<List<TaskResponse>> getAllTasks() {
        return CommonResponse.success(taskDomainService.getAllTasks());
    }

    /**
     * 根据 ID 查询任务详情（含 runs 列表）。运行中的任务由前端每 3 秒轮询本接口更新进度。
     */
    @GetMapping("/{taskId}")
    public CommonResponse<TaskResponse> getTask(@PathVariable Long taskId) {
        return taskDomainService.getTaskById(taskId)
                .map(CommonResponse::success)
                .orElse(CommonResponse.error(404, "Task not found: " + taskId));
    }

    /**
     * 创建测评任务并异步启动执行。校验参数后创建 Task 和 TaskCaseRun，调用 AgentExecutorService 串行执行。
     */
    @PostMapping
    public ResponseEntity<CommonResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse response = taskDomainService.createTask(request);
        agentExecutorService.executeTask((long) response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(response));
    }

    /**
     * 终止指定案例的执行。running 的案例终止进程，queued 的直接标记 cancelled。
     */
    @PostMapping("/{taskId}/runs/{caseId}/terminate")
    public CommonResponse<TaskResponse> terminateRun(
            @PathVariable Long taskId,
            @PathVariable String caseId) {
        agentExecutorService.terminateRun(taskId, caseId);
        return CommonResponse.success(taskDomainService.terminateRun(taskId, caseId));
    }

    /**
     * 获取任务下所有案例的执行记录列表。
     */
    @GetMapping("/{taskId}/runs")
    public CommonResponse<List<CaseRun>> getTaskRuns(@PathVariable Long taskId) {
        return CommonResponse.success(taskDomainService.getRunsByTaskId(taskId));
    }

    /**
     * 发起自动评分。任务状态必须为 completed 且 scoringStatus 不能已是 scoring。
     */
    @PostMapping("/{taskId}/score")
    public CommonResponse<TaskResponse> scoreTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ScoreRequest request) {
        TaskResponse response = taskDomainService.scoreTask(taskId, request);
        scoringClientService.scoreTask(taskId);
        return CommonResponse.success(response);
    }

    /**
     * 向运行中的任务动态添加案例，新案例追加到执行队列末尾。
     */
    @PostMapping("/{taskId}/runs")
    public CommonResponse<TaskResponse> addCases(
            @PathVariable Long taskId,
            @Valid @RequestBody AddCasesRequest request) {
        log.info("Adding {} cases to task {}", request.getCaseIds().size(), taskId);
        List<CaseRun> newRuns = taskDomainService.addCasesToTask(taskId, request.getCaseIds());
        agentExecutorService.enqueueCases(taskId, request.getCaseIds());
        return taskDomainService.getTaskById(taskId)
                .map(t -> CommonResponse.success(t.withRuns(newRuns)))
                .orElse(CommonResponse.error(404, "Task not found: " + taskId));
    }

    /**
     * 从任务中移除指定案例执行记录，标记 removed 并不再参与评分统计。
     */
    @DeleteMapping("/{taskId}/runs/{runId}")
    public CommonResponse<TaskResponse> removeRun(
            @PathVariable Long taskId,
            @PathVariable Long runId,
            @RequestParam(required = false) String reason) {
        log.info("Removing run {} from task {}, reason={}", runId, taskId, reason);
        taskDomainService.removeRun(taskId, runId, reason);
        return taskDomainService.getTaskById(taskId)
                .map(CommonResponse::success)
                .orElse(CommonResponse.error(404, "Task not found: " + taskId));
    }

    /**
     * 重跑单条案例，重置状态为 queued，清除执行结果，attempts+1。
     */
    @PostMapping("/{taskId}/runs/{runId}/rerun")
    public CommonResponse<TaskResponse> rerunCase(
            @PathVariable Long taskId,
            @PathVariable Long runId) {
        log.info("Rerunning case: task={}, run={}", taskId, runId);
        taskDomainService.rerunCase(taskId, runId);
        return taskDomainService.getTaskById(taskId)
                .map(CommonResponse::success)
                .orElse(CommonResponse.error(404, "Task not found: " + taskId));
    }

    /**
     * 取消整个任务，终止所有 running/queued 的案例，已完成的不变。
     */
    @PostMapping("/{taskId}/cancel")
    public CommonResponse<Map<String, String>> cancelTask(@PathVariable Long taskId) {
        log.info("Cancelling task {}", taskId);
        taskDomainService.cancelTask(taskId);
        return CommonResponse.success(Collections.singletonMap("status", "cancelled"));
    }
}
