package com.example.agenteval.domain.model.pojo.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QwenCaseRun {

    private Integer tokenOut;
    private Integer tokenIn;
    private Integer turn;
    private long durationMs;

}
