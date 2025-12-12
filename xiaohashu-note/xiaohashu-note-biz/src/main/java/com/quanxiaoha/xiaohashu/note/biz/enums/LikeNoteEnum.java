package com.quanxiaoha.xiaohashu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeNoteEnum {
    LIKE(1),
    UNLIKE(0);
    private final int type;
}
