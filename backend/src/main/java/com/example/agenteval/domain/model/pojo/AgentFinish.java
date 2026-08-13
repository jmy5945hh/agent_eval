package com.example.agenteval.domain.model.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentFinish {

    private String sessionId;

    private String evalSessionId;

    private String cwd;

    private String error;

    private String errorDetails;

    private String lastAssistantMessage;

}
