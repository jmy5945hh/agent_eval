package com.example.agenteval.infrastructure.enums;

/**
 * 开关状态枚举类
 */
public enum SwitchStatusEnum {

    /**
     * 启动
     */
    ENABLE(1, "enable"),

    /**
     * 禁用
     */
    DISABLED(0, "disabled"),
    ;

    private final Integer status;

    private final String status_name;

    SwitchStatusEnum(Integer status, String status_name) {
        this.status = status;
        this.status_name = status_name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStatus_name() {
        return status_name;
    }
}
