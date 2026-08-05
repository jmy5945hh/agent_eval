package com.example.agenteval.application.dto.request.model;

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
@ApiModel("模型列表请求参数")
public class ModelListRequest extends BasePageRequest {

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称")
    private String modelName;

}
