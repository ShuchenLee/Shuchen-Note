package com.quanxiaoha.xiaohashu.search.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SearchUserRespVO {
    private Long userId;
    private String nickName;
    private String avatar;
    private String xiaohashuId;
    private Integer noteTotal;
    private String fansTotal;
    private String highlightNickName;
}
