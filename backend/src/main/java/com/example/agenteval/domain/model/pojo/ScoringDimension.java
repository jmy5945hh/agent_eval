package com.example.agenteval.domain.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringDimension {
    private String key;
    private String label;
    private Integer weight;
    private String desc;
}
