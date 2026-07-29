package com.example.agenteval.infrastructure.enums;

/**
 * 模型调用类型枚举类
 */
public enum ModelCallTypeEnum {

    /**
     * 根据模式调用
     */
    MODE(1, "mode"),

    /**
     * 根据名称调用
     */
    NAME(2, "name"),
    ;

    private final Integer type;

    private final String type_name;

    ModelCallTypeEnum(Integer type, String type_name) {
        this.type = type;
        this.type_name = type_name;
    }

    public Integer getType() {
        return type;
    }

    public String getType_name() {
        return type_name;
    }
}
