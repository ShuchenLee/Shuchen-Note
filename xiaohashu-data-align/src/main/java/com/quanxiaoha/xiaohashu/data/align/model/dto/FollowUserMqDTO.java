package com.quanxiaoha.xiaohashu.data.align.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUserMqDTO {
    private Long userId;
    private Long followUserId;
    private Integer type;
}
