package com.darc.downbit.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/22-22:38:10
 * @description
 */
public class CommonUtil {
    public static String formatTimeString(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        // 转换为秒
        long seconds = timeDiff / 1000;

        if (seconds < 60) {
            seconds = seconds <= 0 ? 1 : seconds;
            return seconds + "秒前";
        }

        // 转换为分钟
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "分钟前";
        }

        // 转换为小时
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "小时前";
        }

        // 转换为天
        long days = hours / 24;
        if (days < 7) {
            return days + "天前";
        }

        // 超过一周则返回具体日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(timestamp));
    }
}
