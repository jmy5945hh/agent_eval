package com.example.agenteval.domain.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunScore {
    private Map<String, Integer> dims;
    private Map<String, String> comments;
    private String analysis;
    private String note;
    private Boolean edited;
    private String model;
    private String standardVersion;
}
