package com.example.agenteval.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "scoring.cli")
public class ScoringCliConfig {
    private String commandTemplate = "echo 'mock scoring'";
    private int timeoutSeconds = 300;
    private String workDirPrefix = "/tmp/agent-eval/scoring";
    private int maxRetries = 2;
}
