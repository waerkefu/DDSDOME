package com.daijia.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求
 */
@Data
public class LoginRequest implements Serializable {

    /** 登录账号或手机号 */
    @NotBlank(message = "账号不能为空")//字段值 不为`null`
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}