package com.quanxiaoha.xiaohashu.search.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SearchNoteRespVO {
    private Long noteId;
    private String cover;
    private String title;
    private String highlightTitle;
    private String avatar;
    private String nickname;
    private LocalDateTime updateTime;
    private Integer likeTotal;
}
