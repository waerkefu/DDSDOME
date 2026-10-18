package com.daijia.user.dto;

import com.daijia.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    /** 账号 / 手机号 / 昵称 模糊匹配 */
    private String keyword;

    /** 1=客户 2=服务商 3=管理员 */
    private Integer userType;

    /** 1=启用 0=禁用 */
    private Integer status;
}