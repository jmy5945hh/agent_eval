package com.example.agenteval.application.dto.response.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("创建任务请求体")
public class CreateTaskRequest {

    @ApiModelProperty(value = "agent id", required = true)
    @NotNull(message = "agentId不能为空")
    private Integer agentId;

    @ApiModelProperty(value = "agent版本id", required = true)
    @NotNull(message = "agentVersionId不能为空")
    private Integer agentVersionId;

    @ApiModelProperty(value = "模型id", required = true)
    @NotNull(message = "modelId不能为空")
    private Integer modelId;

    @ApiModelProperty(value = "案例id集合", required = true)
    @NotNull(message = "caseIds不能为空")
    private List<Integer> caseIds;

    @ApiModelProperty(value = "评分标准id", required = true)
    @NotNull(message = "scoringStandardId不能为空")
    private Integer scoringStandardId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty("创建者")
    private String createUserName;

}
