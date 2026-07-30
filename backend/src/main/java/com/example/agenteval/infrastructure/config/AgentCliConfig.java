package com.example.agenteval.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "agent.cli")
public class AgentCliConfig {
    private String commandTemplate = "echo 'mock agent execution'";
    private int timeoutSeconds = 1800;
    private String workDirPrefix = "/tmp/agent-eval";
    private int maxRetries = 3;
}
