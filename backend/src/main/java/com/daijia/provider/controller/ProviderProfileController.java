package com.daijia.provider.controller;

import com.daijia.common.Result;
import com.daijia.provider.dto.AcceptSwitchRequest;
import com.daijia.provider.dto.OnlineStatusRequest;
import com.daijia.provider.dto.ProviderVO;
import com.daijia.provider.service.ProviderService;
import com.daijia.security.RequireRole;
import com.daijia.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务商端 - 个人档案管理
 */
@RestController
@RequestMapping("/api/provider/profile")
@RequireRole("PROVIDER")
@RequiredArgsConstructor
public class ProviderProfileController {

    private final ProviderService providerService;

    /** 当前服务商档案 */
    @GetMapping("")
    public Result<ProviderVO> currentProfile() {
        return Result.ok(providerService.currentProfile(UserContext.currentUserId()));
    }

    /** 修改在线状态 */
    @PutMapping("/online")
    public Result<Void> updateOnlineStatus(@Valid @RequestBody OnlineStatusRequest request) {
        providerService.updateOnlineStatus(UserContext.currentUserId(), request.getOnlineStatus());
        return Result.ok();
    }

    /** 修改接单开关 */
    @PutMapping("/accept")
    public Result<Void> updateAcceptSwitch(@Valid @RequestBody AcceptSwitchRequest request) {
        providerService.updateAcceptSwitch(UserContext.currentUserId(), request.getAcceptEnabled());
        return Result.ok();
    }
}
