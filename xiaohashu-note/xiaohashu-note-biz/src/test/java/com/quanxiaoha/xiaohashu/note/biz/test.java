package com.quanxiaoha.xiaohashu.note.biz;

import com.quanxiaoha.xiaohashu.note.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.enums.CollectNoteEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class test {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Test
    void test(){
        log.info("================={}",CollectNoteEnum.COLLECT.getType());
    }
}
