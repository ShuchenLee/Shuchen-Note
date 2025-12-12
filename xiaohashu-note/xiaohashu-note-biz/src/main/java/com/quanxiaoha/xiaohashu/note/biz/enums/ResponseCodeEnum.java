package com.quanxiaoha.xiaohashu.note.biz.enums;

import com.quanxiaoha.xiaohashu.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {
    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("NOTE-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_INVALID("NOTE-10001", "参数错误"),
    // ----------- 业务异常状态码 -----------
    NOTE_TYPE_ERROE("NOTE-20000", "笔记类型异常"),
    NOTE_PUBLISH_ERROR("NOTE-20001", "笔记发布失败"),
    NOTE_NOT_EXIST("NOTE-20002", "笔记不存在"),
    NOTE_PRIVATE("NOTE-20003", "作者已将该笔记设置为仅自己可见"),
    NOTE_UPDATE_ERROR("NOTE-20004", "笔记更新失败"),
    TOPIC_NOT_FOUND("NOTE-20005", "话题信息错误"),
    NOTE_DELETE_ERROR("NOTE-20006", "笔记删除错误"),
    NOTE_SET_PRIVATE_ERROR("NOTE-20007", "笔记设置为隐私错误"),
    NOTE_SET_TOP_ERROR("NOTE-20008", "笔记设置为置顶错误"),
    NOTE_CANT_OPERATE("NOTE-20009", "非笔记作者不能操作"),
    NOTE_HAVE_LIKED("NOTE-200010", "已经点赞过该笔记"),
    ;
    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
