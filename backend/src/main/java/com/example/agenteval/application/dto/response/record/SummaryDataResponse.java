package com.example.agenteval.application.dto.response.record;

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
@ApiModel("查询汇总数据返回体")
public class SummaryDataResponse {

    /**
     * 全部任务
     */
    @ApiModelProperty(value = "全部任务")
    private Integer taskCount;

    /**
     * 完成任务
     */
    @ApiModelProperty(value = "完成任务")
    private Integer finishCount;

    /**
     * 执行中的任务
     */
    @ApiModelProperty(value = "执行中的任务")
    private Integer runCount;

    /**
     * 队列中的案例
     */
    @ApiModelProperty(value = "队列中的案例")
    private Integer queueCases;

    /**
     * 平均得分
     */
    @ApiModelProperty(value = "平均得分")
    private double averageScore;


}
