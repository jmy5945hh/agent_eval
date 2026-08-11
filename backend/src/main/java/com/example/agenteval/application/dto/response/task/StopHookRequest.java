package com.example.agenteval.application.dto.response.task;

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
    private String sessionId;

    @JsonProperty("transcript_path")
    private String transcriptPath;

    @JsonProperty("cwd")
    private String cwd;

    @JsonProperty("hook_event_name")
    private String hookEventName;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("stop_hook_active")
    private String stopHookActive;

    @JsonProperty("last_assistant_message")
    private String lastAssistantMessage;

    @JsonProperty("context_usage")
    private String contextUsage;

    @JsonProperty("context_limit")
    private String contextLimit;

    @JsonProperty("input_tokens")
    private String inputTokens;

}
