package org.example.springboot.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;

import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);
    
    @Resource
    private Client smsClient;
    
    @Value("${aliyun.sms.sign-name}")
    private String signName;
    
    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    @Resource
    private UserMapper userMapper;
    
    /**
     * 发送验证码
     * @param phone 手机号
     * @return 生成的验证码
     */
    public Result<?> sendVerificationCode(String phone) throws UnsupportedEncodingException {
        // 验证手机号格式
        if (!isValidPhoneNumber(phone)) {
            return Result.error("-1","手机号无效");
        }

//        if(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone,phone))!=null){
//            return Result.error("-1","手机号已被注册");
//        }

        String code = generateVerificationCode();

        String SIGN_NAME=new String(signName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("阿里云签名：{},{}",SIGN_NAME,signName);
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
            
            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsResponse response = smsClient.sendSmsWithOptions(request, runtime);
            
            // 添加更详细的错误日志
            log.info("短信发送响应: {}", response.getBody());
            
            if (!"OK".equals(response.getBody().getCode())) {
                String errorCode = response.getBody().getCode();
                String errorMessage = response.getBody().getMessage();
                
                // 特殊处理测试环境的手机号限制错误
                if ("isv.SMS_TEST_NUMBER_LIMIT".equals(errorCode)) {
                    log.warn("测试环境短信发送限制: 手机号 {} 未授权, requestId={}", 
                        phone, response.getBody().getRequestId());
                    return Result.error("-1", "测试环境下，该手机号未授权，请联系管理员添加测试号码");
                }
                
                log.error("短信发送失败: code={}, message={}, requestId={}", 
                    errorCode, errorMessage, response.getBody().getRequestId());
                return Result.error("-1", "短信发送失败: " + errorMessage);
            }
            
            return Result.success(code);
        } catch (Exception e) {
            log.error("发送短信时发生错误", e);
            return Result.error("-1", "短信发送失败，请稍后重试");
        }
    }
    
    /**
     * 生成6位随机验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 验证手机号格式
     */
    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
} 