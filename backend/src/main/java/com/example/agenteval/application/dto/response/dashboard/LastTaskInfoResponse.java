package com.example.agenteval.application.dto.response.dashboard;

import com.example.agenteval.domain.model.pojo.ScoreCommentResult;
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
@ApiModel("最近一次测评")
public class LastTaskInfoResponse {

    @ApiModelProperty("任务id")
    private Integer id;

    @ApiModelProperty("任务名称")
    private String taskName;

    @ApiModelProperty("得分")
    private Integer score;

    @ApiModelProperty("成功率")
    private String successRate;

    @ApiModelProperty("有效率")
    private String effectiveRate;

    @ApiModelProperty("agent名称")
    private String agentName;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("案例数量")
    private Integer caseCount;

    @ApiModelProperty("案例类别数量")
    private Integer categoryCount;

    @ApiModelProperty("完成时间")
    private String finishTime;

    @ApiModelProperty("评分详情")
    private List<ScoreCommentResult> scoreCommentResults;

}
