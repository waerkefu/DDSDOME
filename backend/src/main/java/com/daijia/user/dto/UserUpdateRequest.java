package com.daijia.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息修改请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    private String nickname;

    private String phone;

    private String remark;
}