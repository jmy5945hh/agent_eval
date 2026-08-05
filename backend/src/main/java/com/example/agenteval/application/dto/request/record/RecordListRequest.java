package com.example.agenteval.application.dto.request.record;

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
@ApiModel("查询评测列表请求体")
public class RecordListRequest extends BasePageRequest {

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    /**
     * agent 主键id
     */
    @ApiModelProperty(value = "agent主键id")
    private Integer agentId;

    /**
     * 任务状态
     */
    @ApiModelProperty(value = "任务状态：1-运行中，2-已完成，3-已取消")
    private Integer taskStatus;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
    private String endTime;

}
