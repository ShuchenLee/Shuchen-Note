package com.quanxiaoha.xiaohashu.note.biz.constants;

public interface MQConstants {
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";
    /**
     * Topic 主题：延迟双删 Redis 笔记缓存
     */
    String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";
    String TOPIC_LIKE_NOTE = "LikeNoteTopic";

    String TAG_LIKE = "Like";
    String TAG_UNLIKE = "Unlike";

    String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";

    String TOPIC_COLLECT_NOTE = "CollectNoteTopic";
    String TAG_COLLECT  = "Collect";
    String TAG_UNCOLLECT  = "Uncollect";
    String TOPIC_COUNT_NOTE_COLLECT = "CountNoteCollectTopic";

    String TOPIC_COUNT_PUBLISH = "CountPublishTopic";
    String  TAG_PUBLISH  = "Publish";
    String TAG_UNPUBLISH  = "Unpublish";
}
