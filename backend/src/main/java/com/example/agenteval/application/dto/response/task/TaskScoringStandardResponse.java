package com.example.agenteval.application.dto.response.task;

import com.example.agenteval.domain.model.pojo.ScoringDimension;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "任务评分标准列表")
public class TaskScoringStandardResponse {

    @ApiModelProperty(value = "评分标准id")
    private Integer id;

    @ApiModelProperty(value = "评分标准名称")
    private String scoringStandardName;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "版本说明")
    private String description;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "评分维度列表")
    private List<ScoringDimension> dimensions;

}
