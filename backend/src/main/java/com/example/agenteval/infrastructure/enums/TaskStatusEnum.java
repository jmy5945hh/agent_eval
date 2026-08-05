package com.example.agenteval.infrastructure.enums;

/**
 * 任务执行状态枚举类
 */
public enum TaskStatusEnum {
    /**
     * 运行中
     */
    RUNNING(1, "running", "运行中"),

    /**
     * 已完成
     */
    COMPLETED(2, "completed", "已完成"),

    /**
     * 已取消
     */
    CANCELLED(3, "cancelled", "已取消"),
    ;


    public static final String STATUS_CONSTANT = "status";
    private final Integer status;
    private final String status_name;
    private final String interpretation;

    TaskStatusEnum(Integer status, String status_name, String interpretation) {
        this.status = status;
        this.status_name = status_name;
        this.interpretation = interpretation;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStatus_name() {
        return status_name;
    }

    public String getInterpretation() {
        return interpretation;
    }
}
