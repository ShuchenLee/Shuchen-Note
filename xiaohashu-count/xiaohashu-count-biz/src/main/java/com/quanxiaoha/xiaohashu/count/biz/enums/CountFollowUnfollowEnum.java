package com.quanxiaoha.xiaohashu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum CountFollowUnfollowEnum {
    FOLLOW(1),
    UNFOLLOW(0);
    private final int type;
    public static CountFollowUnfollowEnum valueOf(int type) {
        for (CountFollowUnfollowEnum countFollowUnfollowEnum : CountFollowUnfollowEnum.values()) {
            if (Objects.equals(type, countFollowUnfollowEnum.getType())) {
                return countFollowUnfollowEnum;
            }
        }
        return null;

    }

}
