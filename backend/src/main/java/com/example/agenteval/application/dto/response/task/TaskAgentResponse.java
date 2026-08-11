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
@ApiModel("任务获取可用Agent列表返回体")
public class TaskAgentResponse {

    @ApiModelProperty("agent主键Id")
    private Integer id;

    @ApiModelProperty("Agent名称")
    private String agentName;

    @ApiModelProperty("Agent描述")
    private String description;

    @ApiModelProperty("Agent可用版本数")
    private Integer availableVersions;


}
