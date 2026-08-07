package com.example.agenteval.infrastructure.enums;

public enum ImportanceEnum {
    /**
     * 高
     */
    HIGH(1, "高"),

    /**
     * 中
     */
    MEDIUM(2, "中"),

    /**
     * 低
     */
    LOW(3, "低"),
    ;

    public static final String IMPORTANCE_NAME = "importance";
    private final Integer importance;
    private final String interpretation;

    ImportanceEnum(Integer importance, String interpretation) {
        this.importance = importance;
        this.interpretation = interpretation;
    }

    public Integer getImportance() {
        return importance;
    }

    public String getInterpretation() {
        return interpretation;
    }
}
