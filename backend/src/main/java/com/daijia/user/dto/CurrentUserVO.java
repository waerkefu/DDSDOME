package com.daijia.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录的用户信息（含角色与权限编码）
 */
@Data
//Serializable 接口，用于序列化对象，将对象转换为字节数组，方便在网络上传输
public class CurrentUserVO implements Serializable {

    private Long id;// 用户ID

    private String username;// 用户名

    private String nickname;// 昵称

    private String avatar;// 头像

    /** 1=客户 2=服务商 3=管理员 */
    private Integer userType;

    /** 角色编码集合 */
    private List<String> roles;

    /** 权限编码集合 */
    private List<String> permissions;
}