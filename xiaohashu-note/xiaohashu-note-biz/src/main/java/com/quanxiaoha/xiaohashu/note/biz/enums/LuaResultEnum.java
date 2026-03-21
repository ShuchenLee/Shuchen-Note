package com.quanxiaoha.xiaohashu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

@Getter
@AllArgsConstructor
public enum LuaResultEnum {
    NOT_EXIST(-1L),
    NOT_LIKE(0L),
    NOTE_LIKED(1L);
    private final Long code;

    public static LuaResultEnum valueOf(Long code) {
        for (LuaResultEnum value : LuaResultEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
