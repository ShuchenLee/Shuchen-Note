package com.quanxiaoha.xiaohashu.auth.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ALiYunSmeSender {
    @Resource
    private Client client;
    /**
     * 发送短信
     * @param signName
     * @param templateCode
     * @param phone
     * @param templateParam
     * @return
     */
    public boolean sendMessage(String signName, String templateCode, String phone, String templateParam){
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumbers(phone)
                .setTemplateParam(templateParam);
        RuntimeOptions options = new RuntimeOptions();
        try{
            log.info("开始发送短信，signName {} templateCode {} phone {} templateParam {}", signName, templateCode, phone, templateParam);
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, options);
            log.info("发送短信成功,response {}",sendSmsResponse.toString());
            return true;
        }catch (Exception e){
            log.error("发送短信失败,{}",e.getMessage());
            return false;
        }

    }

}
