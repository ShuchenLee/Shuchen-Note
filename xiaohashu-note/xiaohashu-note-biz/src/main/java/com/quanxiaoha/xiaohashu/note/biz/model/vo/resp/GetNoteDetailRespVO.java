package com.quanxiaoha.xiaohashu.note.biz.model.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetNoteDetailRespVO {
    Long id;
    Integer type;
    String title;
    String content;
    List<String> imgUris;
    Long topicId;
    String topicName;
    Long creatorId;
    String creatorName;
    String avatar;
    String videoUri;
    LocalDateTime updateTime;
    Integer visible;
}
