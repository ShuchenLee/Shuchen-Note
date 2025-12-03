package com.quanxiaoha.xiaohashu.auth.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.constant.RedisConstants;
import com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.quanxiaoha.xiaohashu.auth.service.VerificationCodeService;
import com.quanxiaoha.xiaohashu.auth.util.ALiYunSmeSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ALiYunSmeSender sender;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        //get phone num
        String phoneNum = sendVerificationCodeReqVO.getPhone();
        //construct verification key
        String key = RedisConstants.buildVerificationCodeKey(phoneNum);
        //construct verification code
        //if already send code
        if (redisTemplate.hasKey(key)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        String code = RandomUtil.randomNumbers(6);
        log.info("send code {} to phone {} ",code,phoneNum);
        executor.execute(()->{
            String signName = "阿里云短信测试";
            String templateCode = "SMS_154950909";
            String templateParam = String.format("{\"code\":\"%s\"}", code);
            sender.sendMessage(signName, templateCode, phoneNum, templateParam);
        });
        redisTemplate.opsForValue().set(key,code,3, TimeUnit.MINUTES);
        return Response.success();
    }
}
