package com.daijia.user.controller;

import com.daijia.common.Result;
import com.daijia.security.RequireRole;
import com.daijia.user.dto.PermissionVO;
import com.daijia.user.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 - 权限查询
 */
@RestController
@RequestMapping("/api/admin/permissions")
@RequireRole("ADMIN")
@RequiredArgsConstructor// 构造函数注入依赖
public class AdminPermissionController {

    private final SysRoleService sysRoleService;

    /** 全部权限（按 sortNo 排序） */
    @GetMapping("")
    public Result<List<PermissionVO>> listAll() {
        return Result.ok(sysRoleService.listPermissions());
    }
}
