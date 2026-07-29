package com.example.agenteval.infrastructure.enums;

public enum ImportanceEnum {
    /**
     * 高
     */
    HIGH(1),

    /**
     * 中
     */
    MEDIUM(2),

    /**
     * 低
     */
    LOW(3),
    ;

    private final Integer importance;


    ImportanceEnum(Integer importance) {
        this.importance = importance;
    }

    public Integer getImportance() {
        return importance;
    }
}
