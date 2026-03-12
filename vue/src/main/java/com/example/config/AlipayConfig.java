package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝沙箱配置，在 application.yml 或 application.properties 中配置即可。
 * 只需填写：appId、私钥、支付宝公钥、网关地址、同步/异步回调地址。
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 沙箱 AppID（支付宝开放平台-沙箱应用） */
    private String appId;
    /** 应用私钥（你生成的 RSA2 私钥，可去掉头尾和换行） */
    private String privateKey;
    /** 支付宝公钥（沙箱里“支付宝公钥”，不是应用公钥） */
    private String alipayPublicKey;
    /** 支付宝网关：沙箱用 https://openapi-sandbox.dl.alipaydev.com/gateway.do */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    /** 支付成功后浏览器同步跳转的地址（前端页面或后端再转发） */
    private String returnUrl;
    /** 支付结果异步通知地址（你后端的一个接口，需公网可访问） */
    private String notifyUrl;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public void setAlipayPublicKey(String alipayPublicKey) { this.alipayPublicKey = alipayPublicKey; }
    public String getGatewayUrl() { return gatewayUrl; }
    public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
}
