package org.example.springboot.controller.sms;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.SmsService;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Tag(name = "短信验证码接口")
@RestController
@RequestMapping("/sms")
@CrossOrigin
public class SmsController {

    @Resource
    private SmsService smsService;

    @Operation(summary = "发送短信验证码")
    @GetMapping("/code/{phone}")
    public Result<?> sendVerificationCode(@PathVariable String phone) throws UnsupportedEncodingException {
       return smsService.sendVerificationCode(phone);

    }
} 