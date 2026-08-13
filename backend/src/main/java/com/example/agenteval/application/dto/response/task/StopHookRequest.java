package com.example.agenteval.application.dto.response.task;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopHookRequest {

    @JsonProperty("session_id")
    @Alias("session_id")
    private String sessionId;

    @JsonProperty("transcript_path")
    @Alias("transcript_path")
    private String transcriptPath;

    @JsonProperty("cwd")
    @Alias("cwd")
    private String cwd;

    @JsonProperty("hook_event_name")
    @Alias("hook_event_name")
    private String hookEventName;

    @JsonProperty("timestamp")
    @Alias("timestamp")
    private String timestamp;

    @JsonProperty("stop_hook_active")
    @Alias("stop_hook_active")
    private String stopHookActive;

    @JsonProperty("last_assistant_message")
    @Alias("last_assistant_message")
    private String lastAssistantMessage;

    @JsonProperty("context_usage")
    @Alias("context_usage")
    private String contextUsage;

    @JsonProperty("context_limit")
    @Alias("context_limit")
    private String contextLimit;

    @JsonProperty("input_tokens")
    @Alias("input_tokens")
    private String inputTokens;

    @JsonProperty("error")
    @Alias("error")
    private String error;

    @JsonProperty("error_details")
    @Alias("error_details")
    private String errorDetails;


}
