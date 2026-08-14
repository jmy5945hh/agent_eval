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
@ApiModel("查询评测列表返回体")
public class RecordListResponse {

    /**
     * 任务Id
     */
    @ApiModelProperty("主键id")
    private Integer id;

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    /**
     * agent 名称
     */
    @ApiModelProperty(value = "agent 名称")
    private String agentName;

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称")
    private String modelName;

    /**
     * 案例个数
     */
    @ApiModelProperty(value = "案例个数")
    private Integer caseCount;

    /**
     * 任务状态
     */
    @ApiModelProperty(value = "任务状态：1-运行中，2-已完成，3-已取消")
    private Integer taskStatus;

    /**
     * 评分状态
     */
    @ApiModelProperty(value = "评分状态：1-未评分，2-评分中，3-已评分，4-已确认")
    private Integer scoreStatus;

    /**
     * 任务发起人
     */
    @ApiModelProperty(value = "任务发起人")
    private String taskCreateUserName;

    /**
     * 任务创建时间
     */
    @ApiModelProperty(value = "任务创建时间，格式：yyyy-MM-dd HH:mm:ss")
    private String taskCreateTaskTime;

}
