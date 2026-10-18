package com.daijia.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户
 * 映射数据库表sys_user表
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 密码（PBKDF2 密文，禁止序列化到响应） */
    @JsonIgnore
    private String password;// 密码

    private String nickname;// 昵称

    private String avatar;// 头像

    /** 1=客户 2=服务商 3=管理员 */
    private Integer userType;

    /** 1=启用 0=禁用 */
    private Integer status;

    private LocalDateTime lastLoginTime;// 最后登录时间

    private String remark;// 备注
    
    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除标记（0=未删除 1=已删除，非时间戳）
    @TableLogic//管 底层 SQL ——告诉数据库"这是个软删标记，请自动加上过滤条件"
    @JsonIgnore//管 对外输出 ——告诉 Jackson"这是个内部字段，请不要序列化给前端"
    private Integer deleted;
}