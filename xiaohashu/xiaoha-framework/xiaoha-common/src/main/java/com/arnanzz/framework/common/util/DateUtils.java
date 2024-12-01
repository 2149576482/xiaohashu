package com.arnanzz.framework.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
public class DateUtils {

    /**
     * LocalDateTime 转时间戳
     */
    public static long localDateTime2Timestamp(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

}
