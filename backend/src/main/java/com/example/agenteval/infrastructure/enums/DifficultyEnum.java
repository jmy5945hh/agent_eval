package com.example.agenteval.infrastructure.enums;

/**
 * 难度枚举类
 */
public enum DifficultyEnum {
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

    private final Integer difficulty;

    DifficultyEnum(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDifficulty() {
        return difficulty;
    }
}
