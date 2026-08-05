package com.example.agenteval.application.dto.response.model;

import com.example.agenteval.domain.model.ModelConfigPO;
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
@ApiModel("模型列表返回体")
public class ModelListResponse {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private Integer id;

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称")
    private String modelName;

    /**
     * 模型版本号
     */
    @ApiModelProperty(value = "模型版本号")
    private String version;

    /**
     * 是否启用,默认为true
     */
    @ApiModelProperty(value = "是否启用")
    private boolean enabled;
    /**
     * 是否为评分模型（true=评分, false=测评）,默认为false
     */
    @ApiModelProperty(value = "是否为评分模型")
    private boolean scoring;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间,格式：yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间,格式：yyyy-MM-dd HH:mm:ss")
    private String updateTime;

    public static ModelListResponse from(ModelConfigPO modelConfigPO) {
        return ModelListResponse.builder().modelName(modelConfigPO.getModelName()).id(modelConfigPO.getId())
                .version(modelConfigPO.getVersion()).enabled(1 == modelConfigPO.getEnabled())
                .scoring(1 == modelConfigPO.getScoring()).description(modelConfigPO.getDescription()).build();
    }
}
