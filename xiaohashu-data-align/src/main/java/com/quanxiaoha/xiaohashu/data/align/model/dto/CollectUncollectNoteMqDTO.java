package com.quanxiaoha.xiaohashu.data.align.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollectUncollectNoteMqDTO {
    private Long noteId;
    private Long userId;
    private Long creatorId;
    private LocalDateTime createTime;
    private Integer type;
}
