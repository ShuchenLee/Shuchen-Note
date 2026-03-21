package com.quanxiaoha.xiaohashu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum NoteSortEnum {
    // 最新
    LATEST(0),
    // 最新点赞
    MOST_LIKE(1),
    // 最多评论
    MOST_COMMENT(2),
    // 最多收藏
    MOST_COLLECT(3),
    ;
    private final int code;

    public static NoteSortEnum valueOf(Integer code) {
        for (NoteSortEnum noteSortTypeEnum : NoteSortEnum.values()) {
            if (Objects.equals(code, noteSortTypeEnum.getCode())) {
                return noteSortTypeEnum;
            }
        }
        return null;
    }
}
