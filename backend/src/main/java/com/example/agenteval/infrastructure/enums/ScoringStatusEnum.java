package com.example.agenteval.infrastructure.enums;

/**
 * 评分状态枚举
 */
public enum ScoringStatusEnum {
    /**
     * 未评分
     */
    IDLE(1, "idle"),

    /**
     * 评分中
     */
    SCORING(2, "scoring"),

    /**
     * 已评分
     */
    SCORED(3, "scored"),
    ;

    private final Integer status;

    private final String status_name;

    ScoringStatusEnum(Integer status, String status_name) {
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
