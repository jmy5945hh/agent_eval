package com.example.agenteval.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ModelConfigRequest {
    @NotBlank(message = "模型名称不能为空")
    private String name;

    private String modelType;
    private String endpoint;
    private String authorization;
    private String version;
    private String provider;
    private String tier;
    private String description;
    private Boolean enabled;
    private Boolean scoring;
}
