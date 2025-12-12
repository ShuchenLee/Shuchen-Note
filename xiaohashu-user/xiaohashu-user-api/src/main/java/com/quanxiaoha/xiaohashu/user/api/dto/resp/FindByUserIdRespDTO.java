package com.quanxiaoha.xiaohashu.user.api.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindByUserIdRespDTO {
    private Long userId;
    private String nickName;
    private String avatar;
    private String introduction;

}
