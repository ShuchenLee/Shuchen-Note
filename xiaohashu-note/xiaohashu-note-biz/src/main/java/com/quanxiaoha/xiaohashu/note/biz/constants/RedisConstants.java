package com.quanxiaoha.xiaohashu.note.biz.constants;

public class RedisConstants {
    private static final String noteDetailPrefix = "note:detail:";
    private static final String BLOOM_USER_NOTE_LIKE_LIST_KEY = "bloom:note:likes:";
    private static final String USER_NOTE_LIKE_LIST_KEY = "user:note:likes:";
    private static final String BLOOM_USER_NOTE_COLLECTION_KEY = "bloom:note:collect:";
    private static final String USER_NOTE_COLLECT_KEY = "user:note:collects:";

    public static String buildNoteDetailKey(Long noteId)
    {
        return noteDetailPrefix + noteId;
    }
    public static String buildBloomUserNoteLikeListKey(Long userId)
    {
        return BLOOM_USER_NOTE_LIKE_LIST_KEY + userId;
    }
    public static String buildUserNoteLikesKey(Long userId){
        return USER_NOTE_LIKE_LIST_KEY + userId;
    }
    public static String buildBloomUserNoteCollectKey(Long userId){
        return BLOOM_USER_NOTE_COLLECTION_KEY + userId;
    }
    public static String buildUserNoteCollectKey(Long userId){
        return USER_NOTE_COLLECT_KEY + userId;
    }
}
