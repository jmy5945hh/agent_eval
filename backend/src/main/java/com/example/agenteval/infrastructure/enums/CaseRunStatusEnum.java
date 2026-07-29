package com.example.agenteval.infrastructure.enums;

/**
 * 案例执行状态枚举
 */
public enum CaseRunStatusEnum {

    /**
     * 等待
     */
    QUEUED(1, "queued"),
    /**
     * 运行中
     */
    RUNNING(2, "running"),
    /**
     * 成功
     */
    SUCCESS(3, "success"),
    /**
     * 失败
     */
    FAILED(4, "failed"),
    /**
     * 已取消
     */
    CANCELLED(5, "cancelled"),
    ;

    private final Integer status;

    private final String status_name;

    CaseRunStatusEnum(Integer status, String status_name) {
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
