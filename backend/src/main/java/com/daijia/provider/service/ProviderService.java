package com.daijia.provider.service;

import com.daijia.common.PageResult;
import com.daijia.provider.dto.ProviderQuery;
import com.daijia.provider.dto.ProviderSaveRequest;
import com.daijia.provider.dto.ProviderVO;

/**
 * 服务商档案服务
 */
public interface ProviderService {

    /* -------------------- 管理端 -------------------- */

    /**
     * 服务商分页查询
     */
    PageResult<ProviderVO> page(ProviderQuery query);

    /**
     * 新增服务商档案，返回档案 ID
     */
    Long create(ProviderSaveRequest request);

    /**
     * 修改服务商档案
     */
    void update(Long id, ProviderSaveRequest request);

    /**
     * 修改服务商状态（1=启用 0=禁用）
     */
    void changeStatus(Long id, Integer status);

    /* -------------------- 服务商端 -------------------- */

    /**
     * 查询当前登录服务商的档案
     */
    ProviderVO currentProfile(Long userId);

    /**
     * 修改在线状态
     */
    void updateOnlineStatus(Long userId, Integer onlineStatus);

    /**
     * 修改接单开关
     */
    void updateAcceptSwitch(Long userId, Integer acceptEnabled);
}
