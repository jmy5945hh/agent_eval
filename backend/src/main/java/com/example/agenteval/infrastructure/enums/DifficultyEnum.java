package com.example.agenteval.infrastructure.enums;

/**
 * 难度枚举类
 */
public enum DifficultyEnum {
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

    public static final String DIFFICULTY_NAME = "difficulty";
    private final Integer difficulty;
    private final String interpretation;

    DifficultyEnum(Integer difficulty, String interpretation) {
        this.difficulty = difficulty;
        this.interpretation = interpretation;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public String getInterpretation() {
        return interpretation;
    }
}
