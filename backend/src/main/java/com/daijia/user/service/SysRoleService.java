package com.daijia.user.service;

import com.daijia.user.dto.PermissionVO;
import com.daijia.user.dto.RoleSaveRequest;
import com.daijia.user.dto.RoleVO;

import java.util.List;

/**
 * 角色与权限管理服务
 */
public interface SysRoleService {

    /**
     * 查询全部角色
     */
    List<RoleVO> listAll();

    /**
     * 新增角色，返回角色 ID
     */
    Long create(RoleSaveRequest request);

    /**
     * 修改角色
     */
    void update(Long id, RoleSaveRequest request);

    /**
     * 查询全部权限（按 sortNo 排序）
     */
    List<PermissionVO> listPermissions();

    /**
     * 查询角色已分配的权限 ID 列表
     */
    List<Long> listRolePermissionIds(Long roleId);

    /**
     * 重新分配角色权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}
