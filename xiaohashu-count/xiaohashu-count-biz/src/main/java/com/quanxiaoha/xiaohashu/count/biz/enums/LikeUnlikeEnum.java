package com.quanxiaoha.xiaohashu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

@Getter
@AllArgsConstructor
public enum LikeUnlikeEnum {
    LIKE(1),
    UNLIKE(0);
    private final int type;
    public static LikeUnlikeEnum valueOf(int type) {
        for (LikeUnlikeEnum likeUnlikeEnum : LikeUnlikeEnum.values()) {
            if (likeUnlikeEnum.getType() == type) {
                return likeUnlikeEnum;
            }
        }
        return null;
    }
}
