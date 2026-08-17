package com.example.agenteval.application.dto.response.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Agent分数排行榜返回体")
public class AgentLeaderboardResponse {

    @ApiModelProperty("Agent名称")
    private String agentName;

    @ApiModelProperty("Agent平均分")
    private Integer avgScore;

}
