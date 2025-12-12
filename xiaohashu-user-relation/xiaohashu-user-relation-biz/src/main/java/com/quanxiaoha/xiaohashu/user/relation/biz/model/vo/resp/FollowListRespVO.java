package com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowListRespVO {
    private Long userId;

    private String avatar;

    private String nickname;

    private String introduction;
}
