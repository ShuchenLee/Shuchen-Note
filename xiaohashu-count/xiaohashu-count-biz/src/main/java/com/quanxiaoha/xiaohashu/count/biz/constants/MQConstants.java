package com.quanxiaoha.xiaohashu.count.biz.constants;

public interface MQConstants {
    /**
     * Topic: 关注数计数
     */
    String TOPIC_COUNT_FOLLOWING = "CountFollowingTopic";

    /**
     * Topic: 粉丝数计数
     */
    String TOPIC_COUNT_FANS = "CountFansTopic";

    String TOPIC_COUNT_FANS_DB =  "CountFansDBTopic";
    String TOPIC_COUNT_FOLLOWING_DB =  "CountFOLLOWINGDBTopic";

    String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";
    String TOPIC_COUNT_LIKE_DB = "CountLikeDBTopic";
    String TOPIC_COUNT_COLLECT_DB = "CountCollectDBTopic";
    String TOPIC_COUNT_NOTE_COLLECT = "CountNoteCollectTopic";


    String TOPIC_COUNT_PUBLISH = "CountPublishTopic";
    String  TAG_PUBLISH  = "Publish";
    String TAG_UNPUBLISH  = "Unpublish";


}
