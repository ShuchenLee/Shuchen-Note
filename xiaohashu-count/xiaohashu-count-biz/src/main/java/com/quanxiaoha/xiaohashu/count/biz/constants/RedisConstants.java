package com.quanxiaoha.xiaohashu.count.biz.constants;

public class RedisConstants {
    //user count constants
    private static final String userCountPrefix = "user:count:";
    public  static final String fansTotal = "fansTotal";
    public  static final String followTotal = "followTotal";
    public static final String publishTotal = "noteTotal";

    public static final String noteCountPrefix = "note:count:";
    public  static final String likeTotal = "likeTotal";
    public  static final String collectTotal = "collectTotal";

    public static String buildUserCountKey(Long userId) {
        return userCountPrefix + userId;
    }
    public static String buildNoteCountKey(Long noteId) {
        return  noteCountPrefix + noteId;
    }
}
