package com.quanxiaoha.xiaohashu.count.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountCollectUncollectDTO {
    private Long userId;
    private Long noteId;
    private LocalDateTime createTime;
    private Integer type;
    private Long creatorId;
}
