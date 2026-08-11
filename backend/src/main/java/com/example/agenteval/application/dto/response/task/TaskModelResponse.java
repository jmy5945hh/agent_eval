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
@ApiModel("任务获取模型列表返回体")
public class TaskModelResponse {

    @ApiModelProperty("模型主键Id")
    private Integer id;

    @ApiModelProperty("模型名称")
    private String modelName;


}
