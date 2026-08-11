package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.task.*;
import com.example.agenteval.domain.service.TaskDomainService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * 测评任务控制器，负责任务的创建、查询、执行控制和评分触发。
 * 所有业务逻辑统一委托给 {@link TaskDomainService}。
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Api(tags = "评测任务控制器")
@ApiSupport(order = 6)
public class TaskController {

    private final TaskDomainService taskDomainService;

    @ApiOperation("获取可用Agent列表")
    @GetMapping("/available/agents")
    public ResponseEntity<CommonResponse<List<TaskAgentResponse>>> taskAgentList() {
        return ResponseEntity.ok(CommonResponse.success(taskDomainService.taskAgentList()));
    }

    @ApiOperation("获取可用Agent版本列表")
    @GetMapping("/available/agent/versions/{agentId}")
    public ResponseEntity<CommonResponse<List<TaskAgentVersionResponse>>> taskAgentVersionList(@ApiParam(value = "Agent主键Id", required = true) @PathVariable Integer agentId) {
        return ResponseEntity.ok(CommonResponse.success(taskDomainService.taskAgentVersionList(agentId)));
    }

    @ApiOperation("获取可用模型列表")
    @GetMapping("/available/models")
    public ResponseEntity<CommonResponse<List<TaskModelResponse>>> taskModelList() {
        return ResponseEntity.ok(CommonResponse.success(taskDomainService.taskModelList()));
    }

    @ApiOperation("获取可用评分标准列表")
    @GetMapping("/scoring-standards")
    public ResponseEntity<CommonResponse<List<TaskScoringStandardResponse>>> taskScoringStandardList() {
        return ResponseEntity.ok(CommonResponse.success(taskDomainService.taskScoringStandardList()));
    }

    @ApiOperation("获取任务案例列表")
    @PostMapping("/cases")
    public ResponseEntity<CommonResponse<Page<TaskCaseResponse>>> taskCaseList(@Valid @RequestBody CaseListRequest caseListRequest) {
        return ResponseEntity.ok(CommonResponse.success(taskDomainService.taskCaseList(caseListRequest)));
    }

    @ApiOperation("创建任务")
    @PostMapping("/agent/create")
    public ResponseEntity<CommonResponse<Void>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        taskDomainService.createTask(request);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @ApiIgnore
    @ApiOperation("stop事件hook接口")
    @PostMapping("/agent/hook/stop")
    public ResponseEntity<CommonResponse<Void>> stopHook(@Valid @RequestBody StopHookRequest stopHookRequest) {
        taskDomainService.stopHook(stopHookRequest);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
