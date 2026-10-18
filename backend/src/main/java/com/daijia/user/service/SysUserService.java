package com.daijia.user.service;

import com.daijia.common.PageResult;
import com.daijia.user.dto.UserQuery;
import com.daijia.user.dto.UserUpdateRequest;
import com.daijia.user.dto.UserVO;

import java.util.List;

/**
 * 用户管理服务
 */
public interface SysUserService {

    /**
     * 用户分页查询
     */
    PageResult<UserVO> page(UserQuery query);

    /**
     * 修改用户基本信息
     */
    void update(Long id, UserUpdateRequest request);

    /**
     * 修改用户状态（1=启用 0=禁用）
     */
    void changeStatus(Long id, Integer status);

    /**
     * 重新分配用户角色
     */
    void assignRoles(Long id, List<Long> roleIds);
}
