package com.example.agenteval.application.dto.response.task;

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
@ApiModel("任务获取可用Agent版本列表返回体")
public class TaskAgentVersionResponse {

    @ApiModelProperty("agent版本主键Id")
    private Integer id;

    @ApiModelProperty("Agent版本名称")
    private String agentVersion;

    @ApiModelProperty("描述")
    private String description;

}