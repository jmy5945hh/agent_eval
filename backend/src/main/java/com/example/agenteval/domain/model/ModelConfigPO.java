package com.example.agenteval.domain.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "model_config")
public class ModelConfigPO extends BaseEntity {

    /**
     * 模型名称
     */
    @Column(nullable = true, length = 50, name = "model_name")
    private String modelName;
    /**
     * 模型类型:1-mode，2-name,;
     */
    @Column(nullable = true, name = "model_type")
    private int modelType;
    /**
     * 模型请求地址,;
     */
    @Column(nullable = true, length = 200)
    private String endpoint;
    /**
     * 模型授权api,;
     */
    @Column(nullable = true, length = 200)
    private String authorization;
    /**
     * 模型版本号（如 3.0.2）,;
     */
    @Column(nullable = true, length = 20)
    private String version;
    /**
     * 是否启用:1-enable，0-disable,;
     */
    @Column(nullable = true)
    private byte enabled;
    /**
     * 是否为评分模型（1=评分, 2=测评）,;
     */
    @Column(nullable = true)
    private byte scoring;
    /**
     * 模型说明,;
     */
    @Column(nullable = true, length = 200)
    private String description;

}
