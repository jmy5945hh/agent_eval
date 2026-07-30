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
@Table(name = "scoring_standard")
public class ScoringStandardPO extends BaseEntity {

    /**
     * 版本号（如 v2.0）,;
     */
    @Column(nullable = true, length = 20)
    private String version;
    /**
     * 是否为当前使用版本,;
     */
    @Column(nullable = true, name = "is_current")
    private byte isCurrent;
    /**
     * 版本说明,;
     */
    @Column(nullable = true, length = 200)
    private String note;
    /**
     * 评分维度数组（ScoringDimension[] JSON）,;
     * 数据库里是JSON格式，不指定长度
     */
    @Column(nullable = true)
    private String dimensions;
}
