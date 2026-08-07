package com.example.agenteval.infrastructure.enums;

public enum EnumTypeEnum {
    /**
     * 案例类型
     */
    CaseType(1),
    ;

    private final Integer type;

    EnumTypeEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
