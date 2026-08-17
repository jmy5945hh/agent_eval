package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.response.dashboard.AgentLeaderboardResponse;
import com.example.agenteval.application.dto.response.dashboard.EvalTaskCountingResponse;
import com.example.agenteval.application.dto.response.dashboard.LastTaskInfoResponse;

import java.util.List;

public interface DashboardService {

    /**
     * 评测任务计数
     *
     * @return
     */
    EvalTaskCountingResponse evalTaskCounting();

    /**
     * 获取最后一个评测任务的 ID
     *
     * @return
     */
    LastTaskInfoResponse lastTaskInfo();

    /**
     * 获取Agent分数排行榜
     *
     * @return
     */
    List<AgentLeaderboardResponse> agentLeaderboard();
}
