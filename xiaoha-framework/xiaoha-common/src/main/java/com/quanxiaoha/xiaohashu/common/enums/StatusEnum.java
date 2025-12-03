package com.quanxiaoha.xiaohashu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusEnum {
    // 启用
    ENABLE(0),
    // 禁用
    DISABLED(1);
    private final Integer value;
}
