package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.dashboard.AgentLeaderboardResponse;
import com.example.agenteval.application.dto.response.dashboard.EvalTaskCountingResponse;
import com.example.agenteval.application.dto.response.dashboard.LastTaskInfoResponse;
import com.example.agenteval.domain.service.DashboardService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 工作台控制器，提供聚合统计数据，供 DashboardPage 的指标卡片、Agent 排行榜、最近测评摘要使用。
 * avgScore 和 agentRankings 待评分引擎完成后补充真实计算。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Api(tags = "工作台控制器")
@ApiIgnore
@ApiSupport(order = 8)
public class DashboardController {

    private final DashboardService dashboardService;

    @ApiOperation("获取评测任务统计信息")
    @GetMapping("/counting")
    public ResponseEntity<CommonResponse<EvalTaskCountingResponse>> EvalTaskCounting() {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.evalTaskCounting()));
    }

    @ApiOperation("获取最后一个评测任务的信息")
    @GetMapping("/last/task-info")
    public ResponseEntity<CommonResponse<LastTaskInfoResponse>> lastTaskInfo() {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.lastTaskInfo()));
    }


    @ApiOperation("获取Agent分数排行榜")
    @GetMapping("/agent/leaderboard")
    public ResponseEntity<CommonResponse<List<AgentLeaderboardResponse>>> agentLeaderboard() {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.agentLeaderboard()));
    }
}
