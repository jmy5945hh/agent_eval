package com.example.agenteval.application.dto.response.record;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCaseInfoResponse {

    @ApiModelProperty("轮次")
    private String turn;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("输入token")
    private Integer inputToken;

    @ApiModelProperty("输出token")
    private Integer outputToken;

}
