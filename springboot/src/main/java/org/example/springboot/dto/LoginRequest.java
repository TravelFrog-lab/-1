package org.example.springboot.dto;

import lombok.Data;

/**
 * 登录入参（仅用户名+密码），避免复用 {@link org.example.springboot.entity.User} 导致
 * 手机号等校验与前端只传 username/password 不一致，进而引发绑定/序列化异常。
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
