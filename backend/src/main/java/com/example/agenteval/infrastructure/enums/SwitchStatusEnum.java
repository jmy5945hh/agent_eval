package com.example.agenteval.infrastructure.enums;

/**
 * 开关状态枚举类
 */
public enum SwitchStatusEnum {

    /**
     * 启动
     */
    ENABLE(1, "enable", (byte) 1),

    /**
     * 禁用
     */
    DISABLED(0, "disabled", (byte) 0),
    ;

    private final Integer status;

    private final String statusName;

    private final byte byteStatus;

    SwitchStatusEnum(Integer status, String statusName, byte byteStatus) {
        this.status = status;
        this.statusName = statusName;
        this.byteStatus = byteStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStatusName() {
        return statusName;
    }

    public byte getByteStatus() {
        return byteStatus;
    }
}
