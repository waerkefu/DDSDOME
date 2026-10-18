package com.daijia.user.controller;

import com.daijia.common.Result;
import com.daijia.security.RequireRole;
import com.daijia.user.dto.RolePermissionRequest;
import com.daijia.user.dto.RoleSaveRequest;
import com.daijia.user.dto.RoleVO;
import com.daijia.user.service.SysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 - 角色与权限管理
 */
@RestController
@RequestMapping("/api/admin/roles")// 角色管理接口
@RequireRole("ADMIN")// 仅管理员角色访问
@RequiredArgsConstructor// 构造函数注入依赖
public class AdminRoleController {

    private final SysRoleService sysRoleService;

    /** 全部角色 */
    @GetMapping("")
    public Result<List<RoleVO>> listAll() {
        return Result.ok(sysRoleService.listAll());
    }

    /** 新增角色 */
    @PostMapping("")
    public Result<Long> create(@Valid @RequestBody RoleSaveRequest request) {
        return Result.ok(sysRoleService.create(request));
    }

    /** 修改角色 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        sysRoleService.update(id, request);
        return Result.ok();
    }

    /** 查询角色已分配的权限 ID 列表 */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> listPermissions(@PathVariable Long id) {
        return Result.ok(sysRoleService.listRolePermissionIds(id));
    }

    /** 分配角色权限 */
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionRequest request) {
        sysRoleService.assignPermissions(id, request.getPermissionIds());
        return Result.ok();
    }
}
