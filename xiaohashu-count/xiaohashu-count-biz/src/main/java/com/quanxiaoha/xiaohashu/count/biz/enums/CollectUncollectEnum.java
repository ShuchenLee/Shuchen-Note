package com.quanxiaoha.xiaohashu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectUncollectEnum {
    COLLECT(1),
    UNCOLLECT(0);
    private Integer type;
    public static CollectUncollectEnum typeOf(Integer type){
        for  (CollectUncollectEnum collect : CollectUncollectEnum.values()) {
            if(type==collect.type){
                return collect;
            }
        }
        return null;
    }
}
