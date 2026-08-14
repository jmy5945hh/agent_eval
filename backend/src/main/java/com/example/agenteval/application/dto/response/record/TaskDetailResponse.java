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
@ApiModel("任务详情返回体")
public class TaskDetailResponse {

    @ApiModelProperty("任务id")
    private Integer id;

    @ApiModelProperty("任务名称")
    private String taskName;

    @ApiModelProperty("agent名称")
    private String agentName;

    @ApiModelProperty("agent版本")
    private String agentVersion;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("评分标准名称")
    private String scoringStandardName;

    @ApiModelProperty("执行进度")
    private ExecutionProgress executionProgress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel("执行进度返回体")
    public static class ExecutionProgress {
        @ApiModelProperty("总案例数")
        private Integer totalCase;

        @ApiModelProperty("成功案例数")
        private Integer successCase;

        @ApiModelProperty("失败案例数")
        private Integer failCase;

        @ApiModelProperty("队列案例数")
        private Integer queueCase;
    }

}
