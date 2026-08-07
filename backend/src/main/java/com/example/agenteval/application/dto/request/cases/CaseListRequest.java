package com.example.agenteval.application.dto.request.cases;

import com.example.agenteval.application.dto.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@ApiModel("案例列表请求体")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseListRequest extends BasePageRequest {

    @ApiModelProperty(value = "案例名称")
    private String caseName;

    @ApiModelProperty(value = "案例仓库")
    private String repo;

    @ApiModelProperty(value = "案例分类", allowableValues = "调用枚举接口，类型传“1”获取主键id")
    private Integer category;

}
