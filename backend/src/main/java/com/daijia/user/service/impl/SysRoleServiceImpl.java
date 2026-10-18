package com.daijia.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;
import com.daijia.user.dto.PermissionVO;
import com.daijia.user.dto.RoleSaveRequest;
import com.daijia.user.dto.RoleVO;
import com.daijia.user.entity.SysPermission;
import com.daijia.user.entity.SysRole;
import com.daijia.user.entity.SysRolePermission;
import com.daijia.user.mapper.SysPermissionMapper;
import com.daijia.user.mapper.SysRoleMapper;
import com.daijia.user.mapper.SysRolePermissionMapper;
import com.daijia.user.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色与权限管理服务实现
 */
@Service
@RequiredArgsConstructor// 构造函数注入依赖
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    // 查询全部角色（按 id 排序）
    @Override
    public List<RoleVO> listAll() {
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
        return roles.stream().map(this::toRoleVO).collect(Collectors.toList());
    }

    // 创建角色
    @Override
    public Long create(RoleSaveRequest request) {
        if (!StringUtils.hasText(request.getRoleCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码不能为空");
        }
        Long exists = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, request.getRoleCode()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(1);
        sysRoleMapper.insert(role);
        return role.getId();
    }

    // 更新角色
    @Override
    public void update(Long id, RoleSaveRequest request) {
        SysRole exist = requireRole(id);
        /**检查修改后的角色编码是否已存在
         * 判断条件1：角色编码是不是传了
         * 判断条件2：角色编码是不是变了*/
        if (StringUtils.hasText(request.getRoleCode())
                && !request.getRoleCode().equals(exist.getRoleCode())) {
            // 条件为 true 时，判断角色编码是否重复存在
            Long count = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, request.getRoleCode()));
            if (count != null && count > 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码已存在");
            }
        }

        // 更新角色信息
        SysRole update = new SysRole();
        update.setId(id);
        //如果角色编码有变化，才更新，否则不更新。
        if (StringUtils.hasText(request.getRoleCode())) {
            update.setRoleCode(request.getRoleCode());
        }
        update.setRoleName(request.getRoleName());
        update.setDescription(request.getDescription());
        sysRoleMapper.updateById(update);
    }

    @Override
    // 查询全部权限（按 sortNo 排序）
    public List<PermissionVO> listPermissions() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSortNo)
                .orderByAsc(SysPermission::getId));// 按 sortNo 排序，再按 id 排序
        //把“实体列表”批量转成“VO 列表” 。
        return permissions.stream().map(this::toPermissionVO).collect(Collectors.toList());
    }

    // 查询角色权限（按 id 排序）
    @Override
    public List<Long> listRolePermissionIds(Long roleId) {
        return sysPermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    // 分配角色权限
    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        requireRole(roleId);//先校验角色是否存在
        // 先清空原有权限，再批量写入
        sysRolePermissionMapper.deleteByRoleId(roleId);
        //如果权限列表为空，直接返回。
        if (permissionIds == null) {
            return;
        }
        // 批量写入角色权限
        for (Long permissionId : permissionIds) {
            // 如果权限 id 为空，跳过本轮循环。
            if (permissionId == null) {
                continue;
            }
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            sysRolePermissionMapper.insert(rolePermission);
        }
    }

    // 校验角色是否存在，若不存在则抛出异常。（工具类）
    private SysRole requireRole(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    // 角色实体转 VO（工具类）
    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        return vo;
    }

    // 权限实体转 VO（工具类）
    private PermissionVO toPermissionVO(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setParentId(permission.getParentId());
        vo.setPermCode(permission.getPermCode());
        vo.setPermName(permission.getPermName());
        vo.setPermType(permission.getPermType());
        vo.setPath(permission.getPath());
        vo.setComponent(permission.getComponent());
        vo.setIcon(permission.getIcon());
        vo.setSortNo(permission.getSortNo());
        return vo;
    }
}
