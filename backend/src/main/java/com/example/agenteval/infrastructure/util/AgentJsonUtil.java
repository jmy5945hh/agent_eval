package com.example.agenteval.infrastructure.util;

import cn.hutool.json.JSONObject;

import java.util.Map;

/**
 * agent-json合并工具
 */
public class AgentJsonUtil {

    /**
     * 将 override 中的所有内容合并到 source 中，相同键以 override 为准。
     * 若值为 JSONObject 则递归合并；其他类型（包括 JSONArray）直接覆盖。
     *
     * @param source   被合并的源 JSON 对象（会被修改）
     * @param override 覆盖用的 JSON 对象
     * @return 合并后的 source（即原对象）
     */
    public static JSONObject merge(JSONObject source, JSONObject override) {
        for (Map.Entry<String, Object> entry : override.entrySet()) {
            String key = entry.getKey();
            Object overrideValue = entry.getValue();

            // 如果 override 的值为 JSONObject，且 source 中也存在同键 JSONObject，则递归合并
            if (overrideValue instanceof JSONObject) {
                JSONObject overrideObj = (JSONObject) overrideValue;
                Object sourceValue = source.get(key);
                if (sourceValue instanceof JSONObject) {
                    // 递归合并内部对象
                    merge((JSONObject) sourceValue, overrideObj);
                } else {
                    // 不是 JSONObject 则直接覆盖（新增或替换）
                    source.put(key, overrideObj);
                }
            } else {
                // 其他类型（字符串、数字、数组、布尔等）直接覆盖
                source.put(key, overrideValue);
            }
        }
        return source;
    }
}
