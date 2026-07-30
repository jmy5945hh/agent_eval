package com.example.agenteval.domain.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseRun {
    private String caseId;
    private String status;
    private Integer attempts;
    private Boolean removed;
    private String removeReason;
    private Integer rounds;
    private Integer tokensIn;
    private Integer tokensOut;
    private Long durationMs;
    private ErrorInfo error;
    @Builder.Default
    private List<TrajectoryEntry> trajectory = new ArrayList<>();
    private RunScore score;
}
