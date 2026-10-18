package com.daijia.provider.controller;

import com.daijia.common.PageResult;
import com.daijia.common.Result;
import com.daijia.provider.dto.ProviderQuery;
import com.daijia.provider.dto.ProviderSaveRequest;
import com.daijia.provider.dto.ProviderVO;
import com.daijia.provider.service.ProviderService;
import com.daijia.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 服务商管理
 */
@RestController
@RequestMapping("/api/admin/providers")
@RequireRole("ADMIN")
@RequiredArgsConstructor
public class AdminProviderController {

    private final ProviderService providerService;

    /** 服务商分页查询 */
    @GetMapping("")
    public Result<PageResult<ProviderVO>> page(ProviderQuery query) {
        return Result.ok(providerService.page(query));
    }

    /** 新增服务商档案 */
    @PostMapping("")
    public Result<Long> create(@Valid @RequestBody ProviderSaveRequest request) {
        return Result.ok(providerService.create(request));
    }

    /** 修改服务商档案 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProviderSaveRequest request) {
        providerService.update(id, request);
        return Result.ok();
    }

    /** 修改服务商状态 */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        providerService.changeStatus(id, status);
        return Result.ok();
    }
}
