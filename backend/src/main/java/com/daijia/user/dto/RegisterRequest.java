package com.daijia.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求
 */
@Data
public class RegisterRequest implements Serializable {

    /** 登录账号 */
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 32, message = "账号长度需为 4-32 位")
    private String username;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
    private String password;

    /** 昵称，可选 */
    private String nickname;
}