package com.example.agenteval.infrastructure.util;

import java.util.concurrent.TimeUnit;

public class DateUtil {

    /**
     * 计算时间差
     *
     * @return
     */
    public static String calculateTimeDifference(long diffInMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis) % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("天");
        }
        if (hours > 0) {
            result.append(hours).append("小时");
        }
        if (minutes > 0) {
            result.append(minutes).append("分钟");
        }
        if (seconds > 0 || result.length() == 0) { // 如果结果为空，至少显示秒
            result.append(seconds).append("秒");
        }
        return result.toString();
    }
}
