package com.daijia.user.enums;

import lombok.Getter;

/**
 * 用户类型
 */
@Getter
public enum UserType {
    // 客户
    CUSTOMER(1, "客户"),
    // 服务商
    PROVIDER(2, "服务商"),
    // 管理员
    ADMIN(3, "管理员");

    private final Integer code;// 用户类型编码
    private final String description;// 用户类型描述

    UserType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据用户类型编码获取对应的用户类型枚举值
     *
     * @param code 用户类型编码
     * @return 对应的用户类型枚举值
     */
    public static UserType of(Integer code) {
        for (UserType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}