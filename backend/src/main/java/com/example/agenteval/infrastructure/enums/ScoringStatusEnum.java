package com.example.agenteval.infrastructure.enums;

/**
 * 评分状态枚举
 */
public enum ScoringStatusEnum {
    /**
     * 未评分
     */
    IDLE(1, "idle", "未评分"),

    /**
     * 评分中
     */
    SCORING(2, "scoring", "评分中"),

    /**
     * 已评分
     */
    SCORED(3, "scored", "已评分"),

    /**
     * 已确认
     */
    CONFIRM(4, "confirm", "已确认"),
    ;

    public static final String STATUS_CONSTANT = "status";
    private final Integer status;
    private final String status_name;
    private final String interpretation;

    ScoringStatusEnum(Integer status, String status_name, String interpretation) {
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
