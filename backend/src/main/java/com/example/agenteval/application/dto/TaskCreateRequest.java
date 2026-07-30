package com.example.agenteval.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "Agent ID is required")
    private String agentId;

    private String agentVersionId;

    @NotBlank(message = "Model ID is required")
    private String modelId;

    @NotEmpty(message = "At least one case must be selected")
    private List<String> selectedCases;

    private String scoringStandardId;

    private String taskName;
}
