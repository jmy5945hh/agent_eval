package com.example.agenteval.application.dto.response.model;

import com.example.agenteval.domain.model.ModelConfigPO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelListResponse {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型版本号
     */
    private String version;

    /**
     * 是否启用,默认为true
     */
    private Boolean enabled = true;
    /**
     * 是否为评分模型（true=评分, false=测评）,默认为false
     */
    private Boolean scoring = false;
    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    public static ModelListResponse from(ModelConfigPO modelConfigPO) {
        return ModelListResponse.builder().modelName(modelConfigPO.getModelName()).id(modelConfigPO.getId())
                .version(modelConfigPO.getVersion()).enabled(1 == modelConfigPO.getEnabled())
                .scoring(1 == modelConfigPO.getScoring()).description(modelConfigPO.getDescription()).build();
    }
}
