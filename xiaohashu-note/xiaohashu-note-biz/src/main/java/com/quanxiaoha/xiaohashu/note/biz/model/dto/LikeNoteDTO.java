package com.quanxiaoha.xiaohashu.note.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeNoteDTO {
    private Long userId;
    private Long noteId;
    private LocalDateTime timeStamp;
    private Integer type;
    private Long creatorId;
}
