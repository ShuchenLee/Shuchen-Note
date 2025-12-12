package com.quanxiaoha.xiaohashu.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateUtils {
    public static long getTimeTamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
