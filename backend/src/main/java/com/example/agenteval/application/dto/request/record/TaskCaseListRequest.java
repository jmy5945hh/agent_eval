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
@ApiModel("查询评测任务案例列表请求体")
public class TaskCaseListRequest extends BasePageRequest {

    @ApiModelProperty("状态")
    private Integer state;

}
