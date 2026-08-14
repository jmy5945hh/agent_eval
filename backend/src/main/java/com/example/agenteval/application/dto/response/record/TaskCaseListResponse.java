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
@ApiModel("任务案例列表返回体")
public class TaskCaseListResponse {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("案例名称")
    private String caseName;

    @ApiModelProperty("案例状态")
    private Integer state;

    @ApiModelProperty("轮次")
    private String turn;

    @ApiModelProperty("token")
    private Integer token;

    @ApiModelProperty("耗时")
    private String timeConsuming;

    @ApiModelProperty("评分")
    private String score;


}
