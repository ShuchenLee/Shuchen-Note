package com.quanxiaoha.xiaohashu.gateway.enums;

import com.quanxiaoha.xiaohashu.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum implements BaseExceptionInterface {
    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("OSS-10000", "系统繁忙，请稍后再试"),
    UNAUTHORIZED("OSS-20000", "权限不足"),


    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
