package com.quanxiaoha.xiaohashu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishUnpublishNoteEnum {
    PUBLISH(1),
    UNPUBLISH(0);
    private Integer type;
    public static PublishUnpublishNoteEnum typeOf(Integer type){
        for  (PublishUnpublishNoteEnum publish : PublishUnpublishNoteEnum.values()) {
            if(type==publish.type){
                return publish;
            }
        }
        return null;
    }
}
