package com.example.agenteval.infrastructure.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class EnumUtil {

    /**
     * 根据属性和值找到对应的枚举值
     *
     * @param enumClass
     * @param fieldName
     * @param fieldValue
     * @param <T>
     * @return
     */
    public static <T extends Enum<T>> T findEnumByField(Class<T> enumClass, String fieldName, Object fieldValue) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(enumValue -> {
                    try {
                        Field field = enumClass.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(enumValue);
                        return Objects.equals(value, fieldValue);
                    } catch (Exception e) {
                        return false;
                    }
                }).findFirst().orElse(null);
    }

}
