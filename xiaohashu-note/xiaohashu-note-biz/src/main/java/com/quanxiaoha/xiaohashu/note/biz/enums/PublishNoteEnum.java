package com.quanxiaoha.xiaohashu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishNoteEnum {
    PUBLISH(1),
    UNPUBLISH(0);
    private final int type;

}
