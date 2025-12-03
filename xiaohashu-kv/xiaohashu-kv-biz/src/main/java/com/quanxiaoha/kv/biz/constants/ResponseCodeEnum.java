package com.quanxiaoha.kv.biz.constants;

import com.quanxiaoha.xiaohashu.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {
    //通用状态异常码
    SYSTEM_ERROR("KV-10000","出错啦，后台小哥正在努力修复中..."),
    PARAM_INVALID("KV-100001","参数异常"),
    //业务状态异常码
    NOTE_NOT_EXIST("KV-20000","笔记不存在");
    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
