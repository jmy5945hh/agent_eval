package com.example.agenteval.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentScorePO {

    private Integer agentId;
    private Long count;
    private BigDecimal sumAvgScore;

}
