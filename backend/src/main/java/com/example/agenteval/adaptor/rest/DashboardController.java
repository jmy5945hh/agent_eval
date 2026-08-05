package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台控制器，提供聚合统计数据，供 DashboardPage 的指标卡片、Agent 排行榜、最近测评摘要使用。
 * avgScore 和 agentRankings 待评分引擎完成后补充真实计算。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Api(tags = "工作台控制器")
@ApiIgnore
public class DashboardController {

    private final EvaluationTaskPORespository taskRepository;
    private final EvaluationCasePORespository caseRepository;
    private final TaskCaseRunPORespository caseRunRepository;

    /**
     * 查询工作台聚合统计数据：各状态任务数、案例总数、成功率、平均分、Agent 排名。
     */
    @GetMapping("/stats")
    public CommonResponse<Map<String, Object>> getStats() {
        List<EvaluationTaskPO> allTasks = taskRepository.findAll();
        int total = allTasks.size();
        int running = (int) allTasks.stream().filter(t -> "running".equals(t.getStatus())).count();
        int completed = (int) allTasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
        int cancelled = (int) allTasks.stream().filter(t -> "cancelled".equals(t.getStatus())).count();

        List<TaskCaseRunPO> allRuns = caseRunRepository.findAll();
        long totalRuns = allRuns.size();
        long successRuns = allRuns.stream().filter(r -> "success".equals(r.getStatus())).count();
        double successRate = totalRuns > 0 ? Math.round(successRuns * 1000.0 / totalRuns) / 10.0 : 0.0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTasks", total);
        stats.put("runningTasks", running);
        stats.put("completedTasks", completed);
        stats.put("cancelledTasks", cancelled);
        stats.put("totalCases", caseRepository.count());
        stats.put("avgScore", 0.0);
        stats.put("successRate", successRate);
        stats.put("agentRankings", new ArrayList<>());

        return CommonResponse.success(stats);
    }
}
