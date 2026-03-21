package com.quanxiaoha.xiaohashu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NoteTypeEnum {
    IMAGE_TEXT(0),
    VIDEO(1);
    private final Integer type;
    public static NoteTypeEnum getNoteTypeEnum(Integer type){
        for (NoteTypeEnum noteTypeEnum : NoteTypeEnum.values()) {
            if (noteTypeEnum.getType().equals(type)) {
                return noteTypeEnum;
            }
        }
        return null;
    }

}
