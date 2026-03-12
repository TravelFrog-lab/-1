package org.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String publicKey;
    private String gatewayUrl;
    private String charset = "UTF-8";
    private String signType = "RSA2";
    private String format = "JSON";
    private String notifyUrl;
    private String returnUrl;
}
