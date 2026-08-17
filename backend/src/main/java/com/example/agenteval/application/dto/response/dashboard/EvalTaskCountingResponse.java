package com.example.agenteval.application.dto.response.dashboard;

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
@ApiModel("评测任务计数返回体")
public class EvalTaskCountingResponse {

    /**
     * 累计
     */
    @ApiModelProperty("累计评测数")
    private Integer cumulative;

    /**
     * 沉淀
     */
    @ApiModelProperty("沉淀评测数")
    private Integer precipitate;

}
