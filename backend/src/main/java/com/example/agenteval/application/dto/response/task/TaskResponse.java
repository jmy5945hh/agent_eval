package com.example.agenteval.application.dto.response.task;

import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.pojo.CaseRun;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class TaskResponse {
    private int id;
    private String name;
    private int agentId;
    private int agentVersionId;
    private int modelId;
    private String creator;
    private String createdAt;
    private int status;
    private int scoringModelId;
    private int scoringStatus;
    private BigDecimal avgScore;
    private String scoredAt;
    private List<CaseRun> runs;

    public static TaskResponse from(EvaluationTaskPO task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getTaskName())
                .agentId(task.getAgentId())
                .agentVersionId(task.getAgentVersionId())
                .modelId(task.getModelId())
                .creator(task.getCreateUserName())
                .createdAt(task.getCreateTime() != null ? task.getCreateTime().toString() : null)
                .status(task.getStatus())
                .scoringModelId(task.getScoringModelId())
                .scoringStatus(task.getScoringStatus())
                .avgScore(task.getAvgScore())
                .scoredAt(task.getScoreTime() != null ? task.getScoreTime().toString() : null)
                .runs(Collections.emptyList())
                .build();
    }

    public TaskResponse withRuns(List<CaseRun> runs) {
        this.runs = runs;
        return this;
    }
}
