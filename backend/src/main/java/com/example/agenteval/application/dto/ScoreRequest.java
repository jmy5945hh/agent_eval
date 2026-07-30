package com.example.agenteval.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ScoreRequest {
    @NotBlank(message = "Scoring model ID is required")
    private String scoringModelId;

    @NotBlank(message = "Standard version is required")
    private String standardVersion;
}
