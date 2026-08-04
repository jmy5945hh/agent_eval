package com.example.agenteval.application.dto.request.agent;

import com.example.agenteval.application.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AgentListRequest extends BasePageRequest {

    /**
     * agent名称
     */
    private String agentName;

}
