package com.quanxiaoha.xiaohashu.user.biz;

import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.UserDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.UserDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@SpringBootTest
public class test {
    @Resource
    private UserDOMapper userDOMapper;
    @Test
    void test(){
        List<Long> idList = new ArrayList<>();
        idList.add(2100L);
        List<FindByUserIdRespDTO> findByUserIdRespDTOS = userDOMapper.selectUsersByIds(idList);
        log.info("==========={}",findByUserIdRespDTOS);
    }
}
