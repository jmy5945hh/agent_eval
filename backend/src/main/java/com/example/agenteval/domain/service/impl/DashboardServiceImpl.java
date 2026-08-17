package com.example.agenteval.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.example.agenteval.application.dto.response.dashboard.AgentLeaderboardResponse;
import com.example.agenteval.application.dto.response.dashboard.EvalTaskCountingResponse;
import com.example.agenteval.application.dto.response.dashboard.LastTaskInfoResponse;
import com.example.agenteval.domain.model.*;
import com.example.agenteval.domain.repository.*;
import com.example.agenteval.domain.service.DashboardService;
import com.example.agenteval.infrastructure.enums.CaseRunStatusEnum;
import com.example.agenteval.infrastructure.enums.TaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EvaluationTaskPORespository evaluationTaskPORespository;
    private final AgentInfoPORespository agentInfoPORespository;
    private final ModelConfigPORespository modelConfigPORespository;
    private final TaskCaseRunPORespository taskCaseRunPORespository;
    private final EvaluationCasePORespository evaluationCasePORespository;

    @Override
    public EvalTaskCountingResponse evalTaskCounting() {
        //累计
        long count = evaluationTaskPORespository.count();
        return EvalTaskCountingResponse.builder().cumulative((int) count).build();
    }

    @Override
    public LastTaskInfoResponse lastTaskInfo() {
        EvaluationTaskPO evaluationTaskPO = evaluationTaskPORespository.findTopFirstByStatusOrderByUpdateTimeDesc(TaskStatusEnum.COMPLETED.getStatus());
        //agent
        AgentInfoPO agentInfoPO = agentInfoPORespository.findById(evaluationTaskPO.getAgentId()).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + evaluationTaskPO.getAgentId()));
        //model
        ModelConfigPO modelConfigPO = modelConfigPORespository.findById(evaluationTaskPO.getModelId()).orElseThrow(() -> new IllegalArgumentException("模型不存在: " + evaluationTaskPO.getModelId()));
        //案例
        List<TaskCaseRunPO> taskCaseRunPOS = taskCaseRunPORespository.findByTaskId(evaluationTaskPO.getId());
        List<Integer> caseIds = taskCaseRunPOS.stream().map(TaskCaseRunPO::getCaseId).collect(Collectors.toList());
        //案例类别
        List<EvaluationCasePO> casePOList = evaluationCasePORespository.findByIdIn(caseIds);
        long categoryCount = casePOList.stream().map(EvaluationCasePO::getCategory).distinct().count();
        //成功率
        List<TaskCaseRunPO> successTaskCase = taskCaseRunPOS.stream().filter(item -> CaseRunStatusEnum.SUCCESS.getStatus().equals(item.getStatus())).collect(Collectors.toList());
        String successRepo = "0%";
        if (!successTaskCase.isEmpty()) {
            successRepo = BigDecimal.valueOf(successTaskCase.size()).divide(BigDecimal.valueOf(taskCaseRunPOS.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().concat("%");
        }
        //todo 有效率,各维度
        return LastTaskInfoResponse.builder().id(evaluationTaskPO.getId()).taskName(evaluationTaskPO.getTaskName()).agentName(agentInfoPO.getAgentName())
                .modelName(modelConfigPO.getModelName()).score(evaluationTaskPO.getAvgScore().intValue()).successRate(successRepo).finishTime(DateUtil.format(evaluationTaskPO.getUpdateTime(), DatePattern.NORM_DATETIME_PATTERN))
                .caseCount(taskCaseRunPOS.size()).categoryCount((int) categoryCount).build();
    }

    @Override
    public List<AgentLeaderboardResponse> agentLeaderboard() {
        List<AgentScorePO> agentScore = evaluationTaskPORespository.findAgentScore();
        if (CollUtil.isEmpty(agentScore)) {
            return new ArrayList<>();
        }
        List<AgentInfoPO> agentInfoPOList = agentInfoPORespository.findAll();
        Map<Integer, AgentInfoPO> agentInfoPOMap = agentInfoPOList.stream().collect(Collectors.toMap(AgentInfoPO::getId, Function.identity()));
        List<AgentLeaderboardResponse> returnList = new ArrayList<>();
        agentScore.forEach(item -> {
            int score = item.getSumAvgScore().divide(new BigDecimal(item.getCount()), 0, RoundingMode.CEILING).intValue();
            AgentInfoPO agentInfoPO = agentInfoPOMap.get(item.getAgentId());
            returnList.add(AgentLeaderboardResponse.builder().agentName(agentInfoPO.getAgentName()).avgScore(score).build());
        });
        return returnList;
    }
}
