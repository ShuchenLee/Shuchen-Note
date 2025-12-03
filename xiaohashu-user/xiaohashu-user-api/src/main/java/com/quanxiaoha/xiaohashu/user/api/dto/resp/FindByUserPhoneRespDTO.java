package com.quanxiaoha.xiaohashu.user.api.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindByUserPhoneRespDTO {
    private Long id;
    private String password;
}
