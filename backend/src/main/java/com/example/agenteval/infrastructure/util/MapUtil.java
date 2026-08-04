package com.example.agenteval.infrastructure.util;

public class MapUtil {

    /**
     * 将 Boolean 映射为 byte。
     *
     * @param value        Boolean 值
     * @param defaultValue 为 null 时的默认值
     * @return 1 或 0
     */
    public static byte mapBoolean(Boolean value, boolean defaultValue) {
        boolean result = value != null ? value : defaultValue;
        return result ? (byte) 1 : (byte) 0;
    }
}
