package com.quanxiaoha.xiaohashu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectNoteEnum {
    COLLECT(1),
    UNCOLLECT(0);
    private final int type;
}
