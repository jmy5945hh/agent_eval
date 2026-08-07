package com.example.agenteval.domain.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "enum_info")
@DynamicInsert
public class EnumInfoPO extends BaseEntity {

    /**
     * 枚举键
     */
    @Column(nullable = true, length = 100, name = "enum_key")
    private String enumKey;
    /**
     * 枚举值
     */
    @Column(nullable = true, length = 500, name = "enum_value")
    private String enumValue;

    /**
     * 枚举类型
     */
    @Column(nullable = true, name = "enum_type")
    private Integer enumType;

}
