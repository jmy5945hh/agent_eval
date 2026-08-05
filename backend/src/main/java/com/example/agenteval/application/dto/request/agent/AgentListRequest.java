package com.example.agenteval.application.dto.request.agent;

import com.example.agenteval.application.dto.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("查询Agent列表请求体")
public class AgentListRequest extends BasePageRequest {

    /**
     * agent名称
     */
    @ApiModelProperty(value = "agent名称")
    private String agentName;

}
