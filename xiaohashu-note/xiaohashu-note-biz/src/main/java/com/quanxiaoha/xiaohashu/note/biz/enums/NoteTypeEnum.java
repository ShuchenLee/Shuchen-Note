package com.quanxiaoha.xiaohashu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum NoteTypeEnum {
    IMAGE_TEXT(0,"图文"),
    VIDEO(1,"视频"),
    ;
    private final Integer code;
    private final String desc;
    //if type is valid
    public static boolean isValid(Integer type){
        for(NoteTypeEnum noteTypeEnum : NoteTypeEnum.values()){
            if(Objects.equals(noteTypeEnum.getCode(), type)){
                return true;
            }
        }
        return false;
    }
    public static NoteTypeEnum getNoteType(Integer type){
        for(NoteTypeEnum noteTypeEnum : NoteTypeEnum.values()){
            if(Objects.equals(noteTypeEnum.getCode(), type)){
                return noteTypeEnum;
            }
        }
        return null;
    }
}
