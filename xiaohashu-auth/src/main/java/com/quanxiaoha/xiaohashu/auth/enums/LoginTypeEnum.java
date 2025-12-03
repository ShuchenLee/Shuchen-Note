package com.quanxiaoha.xiaohashu.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    // 验证码
    VERIFICATION_CODE(1),
    // 密码
    PASSWORD(2);
    //
    private final Integer value;
    public static LoginTypeEnum valueOf(Integer value) {
        for (LoginTypeEnum loginTypeEnum : LoginTypeEnum.values()) {
            if (loginTypeEnum.getValue().equals(value)) {
                return loginTypeEnum;
            }
        }
        return null;
    }
}
