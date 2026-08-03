package com.example.agenteval.application.dto.response.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelInfoResponse {

    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 模型类型:1-mode，2-name
     */

    private Integer modelType;
    /**
     * 地址
     */
    private String endpoint;
    /**
     * 鉴权
     */
    private String authorization;
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
     * 修改时间
     */
    private String updateTime;
}
