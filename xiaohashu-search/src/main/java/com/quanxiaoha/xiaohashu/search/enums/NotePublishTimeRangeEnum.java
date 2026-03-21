package com.quanxiaoha.xiaohashu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum NotePublishTimeRangeEnum {
    ONE_DAY(0),
    ONE_WEEK(1),
    HALF_YEAR(2);
    private final Integer code;
    public static NotePublishTimeRangeEnum valueOf(Integer code) {
        for (NotePublishTimeRangeEnum notePublishTimeRangeEnum : NotePublishTimeRangeEnum.values()) {
            if (Objects.equals(code, notePublishTimeRangeEnum.getCode())) {
                return notePublishTimeRangeEnum;
            }
        }
        return null;
    }
}
