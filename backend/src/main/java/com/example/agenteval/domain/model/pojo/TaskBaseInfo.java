package com.example.agenteval.domain.model.pojo;

import com.example.agenteval.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskBaseInfo {

    private AgentInfoPO agentInfoPO;

    private AgentVersionPO agentVersionPO;

    private ModelConfigPO modelConfigPO;

    private List<EvaluationCasePO> evaluationCasePOS;

    private ScoringStandardPO scoringStandardPO;

}
