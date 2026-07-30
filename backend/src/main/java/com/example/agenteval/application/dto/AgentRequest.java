package com.example.agenteval.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AgentRequest {
    @NotBlank(message = "Agent 名称不能为空")
    private String name;

    private String version;
    private String vendor;
    private String description;
    private String startCmd;
    private String executorType;
    private String status;
}
