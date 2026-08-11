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
@ApiModel(value = "任务案例列表返回体")
public class TaskCaseResponse {

    @ApiModelProperty(value = "案例id")
    private Integer id;

    @ApiModelProperty(value = "案例名称")
    private String caseName;

    @ApiModelProperty(value = "案例分类")
    private String category;

    @ApiModelProperty(value = "仓库")
    private String repo;

    @ApiModelProperty(value = "分支")
    private String branch;

    @ApiModelProperty(value = "案例难度")
    private String difficulty;
}
