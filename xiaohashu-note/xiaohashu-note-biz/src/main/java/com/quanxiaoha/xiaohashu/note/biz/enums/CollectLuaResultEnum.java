package com.quanxiaoha.xiaohashu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectLuaResultEnum {
    NOT_EXIST(-1L),
    NOT_COLLECTED(0L),
    NOTE_COLLECTED(1L);
    private final Long code;

    public static CollectLuaResultEnum valueOf(Long code) {
        for (CollectLuaResultEnum value : CollectLuaResultEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
