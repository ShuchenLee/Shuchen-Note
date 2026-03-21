package com.quanxiaoha.xiaohashu.common.util;

import com.quanxiaoha.xiaohashu.common.constant.DateConstants;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateUtils {
    public static long getTimeTamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    /**
     * LocalDateTime 转 String 字符串
     * @param time
     * @return
     */
    public static String localDateTime2String(LocalDateTime time) {
        return time.format(DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
    }
}
