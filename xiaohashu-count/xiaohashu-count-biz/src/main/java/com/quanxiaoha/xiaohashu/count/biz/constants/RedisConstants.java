package com.quanxiaoha.xiaohashu.count.biz.constants;

public class RedisConstants {
    private static final String userCountPrefix = "user:count:";
    public  static final String fansTotal = "fansTotal";
    public  static final String followTotal = "followTotal";

    public static String buildUserCountKey(Long userId) {
        return userCountPrefix + userId;
    }
}
