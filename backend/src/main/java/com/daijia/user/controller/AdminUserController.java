package com.daijia.user.controller;

import com.daijia.common.PageResult;
import com.daijia.common.Result;
import com.daijia.security.RequireRole;
import com.daijia.user.dto.UserQuery;
import com.daijia.user.dto.UserRoleRequest;
import com.daijia.user.dto.UserUpdateRequest;
import com.daijia.user.dto.UserVO;
import com.daijia.user.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 用户管理
 */
@RestController
@RequestMapping("/api/admin/users")
@RequireRole("ADMIN")
@RequiredArgsConstructor
public class AdminUserController {

    private final SysUserService sysUserService;

    /** 用户分页查询 */
    @GetMapping("")
    public Result<PageResult<UserVO>> page(UserQuery query) {
        return Result.ok(sysUserService.page(query));
    }

    /** 修改用户基本信息 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        sysUserService.update(id, request);
        return Result.ok();
    }

    /** 修改用户状态 */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysUserService.changeStatus(id, status);
        return Result.ok();
    }

    /** 分配用户角色 */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        sysUserService.assignRoles(id, request.getRoleIds());
        return Result.ok();
    }
}
