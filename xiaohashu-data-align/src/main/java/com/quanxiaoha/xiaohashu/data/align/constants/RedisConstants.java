package com.quanxiaoha.xiaohashu.data.align.constants;

import java.time.LocalDate;

public class RedisConstants {
    private static final String BLOOM_TODAY_NOTE_LIKE_NOTE_ID_LIST_KEY = "bloom:dataAlign:note:likes:noteIds";
    private static final String BLOOM_TODAY_NOTE_LIKE_USER_ID_LIST_KEY = "bloom:dataAlign:note:likes:userIds";
    private static final String BLOOM_TODAY_NOTE_COLLECT_NOTE_ID_LIST_KEY = "bloom:dataAlign:note:collections:noteIds";
    private static final String BLOOM_TODAY_NOTE_COLLECT_USER_ID_LIST_KEY = "bloom:dataAlign:note:collections:userIds";
    private static final String BLOOM_TODAY_NOTE_PUBLISH_LIST_KEY = "bloom:dataAlign:note:publish:";
    private static final String BLOOM_TODAY_USER_FOLLOW_LIST_KEY = "bloom:dataAlign:user:follow:";
    private static final String BLOOM_TODAY_USER_FANS_LIST_KEY = "bloom:dataAlign:user:fans:";
    private static final String USER_COUNT_PREFIX = "user:count";
    public static final String FOLLOW_TOTAL = "followingTotal";
    public static String buildBloomUserNoteLikeNoteIdListKey(String date) {
        return BLOOM_TODAY_NOTE_LIKE_NOTE_ID_LIST_KEY + date;
    }
    public static String buildBloomUserNoteLikeUserIdListKey(String date) {
        return BLOOM_TODAY_NOTE_LIKE_USER_ID_LIST_KEY + date;
    }
    public static String buildBloomUserNoteCollectNoteIdListKey(String date) {
        return BLOOM_TODAY_NOTE_COLLECT_NOTE_ID_LIST_KEY + date;
    }
    public static String buildBloomUserNoteCollectUserIdListKey(String date) {
        return BLOOM_TODAY_NOTE_COLLECT_USER_ID_LIST_KEY + date;
    }
    public static String buildBloomUserNotePublishListKey(String date) {
        return BLOOM_TODAY_NOTE_PUBLISH_LIST_KEY + date;
    }
    public static String buildBloomUserFansListKey(String date) {
        return BLOOM_TODAY_USER_FANS_LIST_KEY + date;
    }
    public static String buildBloomUserFollowListKey(String date) {

        return BLOOM_TODAY_USER_FOLLOW_LIST_KEY + date;
    }
    public static String buildUserCountPrefix(Long userId) {
        return USER_COUNT_PREFIX + userId;
    }
}
