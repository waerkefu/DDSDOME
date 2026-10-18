package com.daijia.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.daijia.common.BusinessException;
import com.daijia.common.NoGenerator;
import com.daijia.common.PageResult;
import com.daijia.common.ResultCode;
import com.daijia.provider.dto.ProviderQuery;
import com.daijia.provider.dto.ProviderSaveRequest;
import com.daijia.provider.dto.ProviderVO;
import com.daijia.provider.entity.Provider;
import com.daijia.provider.mapper.ProviderMapper;
import com.daijia.provider.service.ProviderService;
import com.daijia.user.entity.SysUser;
import com.daijia.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务商档案服务实现
 */
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderMapper providerMapper;
    private final SysUserMapper sysUserMapper;

/* -------------------- 管理端 -------------------- */
    // 分页查询服务商档案
    @Override
    public PageResult<ProviderVO> page(ProviderQuery query) {
        /**
         * 创建一个空的查询条件构造器
         * 拼 SQL 条件
         * 一个 条件收集器
         * 初始为空，后面每调用一次`.like()` 、`.eq()` 就往里塞一个条件
         */
        LambdaQueryWrapper<Provider> wrapper = new LambdaQueryWrapper<>();
        //如果有查询关键词
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword();//创建字符串对象接收查询关键词
            //根据查询关键词查询服务商档案
            wrapper.and(w -> w.like(Provider::getRealName, keyword)
                    .or().like(Provider::getPhone, keyword)
                    .or().like(Provider::getProviderNo, keyword));
        }
        wrapper.eq(query.getStatus() != null, Provider::getStatus, query.getStatus());
        wrapper.eq(query.getAuditStatus() != null, Provider::getAuditStatus, query.getAuditStatus());
        wrapper.eq(StringUtils.hasText(query.getCityCode()), Provider::getCityCode, query.getCityCode());
        wrapper.orderByDesc(Provider::getId);

        //去数据库查询服务商档案
        Page<Provider> page = providerMapper.selectPage(
                new Page<>(query.safePageNum(), query.safePageSize()), wrapper);
        //将查询结果转换为VO对象
        List<ProviderVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        //使用工具包分页方法创建分页结果
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    // 创建服务商档案
    @Override
    public Long create(ProviderSaveRequest request) {
        Provider provider = new Provider();
        provider.setUserId(request.getUserId());
        provider.setProviderNo(NoGenerator.providerNo());
        provider.setRealName(request.getRealName());
        provider.setPhone(request.getPhone());
        provider.setIdCardNo(request.getIdCardNo());
        provider.setServiceTypes(request.getServiceTypes());
        provider.setCityCode(request.getCityCode());
        provider.setVehiclePlate(request.getVehiclePlate());
        provider.setVehicleModel(request.getVehicleModel());
        provider.setDrivingYears(request.getDrivingYears() == null ? 0 : request.getDrivingYears());
        provider.setOnlineStatus(0);
        provider.setAcceptEnabled(0);
        provider.setAuditStatus(1);
        provider.setStatus(1);
        provider.setRemark(request.getRemark());
        providerMapper.insert(provider);
        return provider.getId();
    }

    // 更新服务商档案
    @Override
    public void update(Long id, ProviderSaveRequest request) {
        requireProvider(id);//校验服务商档案是否存在
        Provider update = new Provider();
        update.setId(id);
        update.setRealName(request.getRealName());
        update.setPhone(request.getPhone());
        update.setIdCardNo(request.getIdCardNo());
        update.setServiceTypes(request.getServiceTypes());
        update.setCityCode(request.getCityCode());
        update.setVehiclePlate(request.getVehiclePlate());
        update.setVehicleModel(request.getVehicleModel());
        update.setDrivingYears(request.getDrivingYears());
        update.setRemark(request.getRemark());
        providerMapper.updateById(update);
    }

    // 更新服务商档案状态
    @Override
    public void changeStatus(Long id, Integer status) {
        assertStatus(status);
        requireProvider(id);
        Provider update = new Provider();
        update.setId(id);
        update.setStatus(status);
        providerMapper.updateById(update);
    }

/* -------------------- 服务商端 -------------------- */
    // 查询服务商档案详情
    @Override
    public ProviderVO currentProfile(Long userId) {
        return toVO(requireProviderByUserId(userId));
    }

    // 更新服务商档案在线状态
    @Override
    public void updateOnlineStatus(Long userId, Integer onlineStatus) {
        assertStatus(onlineStatus);
        Provider provider = requireProviderByUserId(userId);
        Provider update = new Provider();
        update.setId(provider.getId());
        update.setOnlineStatus(onlineStatus);
        providerMapper.updateById(update);
    }

    // 更新服务商档案接单状态
    @Override
    public void updateAcceptSwitch(Long userId, Integer acceptEnabled) {
        assertStatus(acceptEnabled);
        Provider provider = requireProviderByUserId(userId);
        Provider update = new Provider();
        update.setId(provider.getId());
        update.setAcceptEnabled(acceptEnabled);
        providerMapper.updateById(update);
    }


/* -------------------- 私有工具方法，供上面的业务逻辑复用 -------------------- */
    // 校验服务商档案是否存在
    private Provider requireProvider(Long id) {
        Provider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BusinessException(ResultCode.PROVIDER_NOT_FOUND);
        }
        return provider;
    }

    // 校验服务商档案是否存在
    private Provider requireProviderByUserId(Long userId) {
        Provider provider = providerMapper.selectByUserId(userId);
        if (provider == null) {
            throw new BusinessException(ResultCode.PROVIDER_NOT_FOUND);
        }
        return provider;
    }

    // 校验服务商档案状态是否有效
    private void assertStatus(Integer status) {
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值只能为 0 或 1");
        }
    }

    // 转换服务商档案为VO
    private ProviderVO toVO(Provider provider) {
        ProviderVO vo = new ProviderVO();
        vo.setId(provider.getId());
        vo.setUserId(provider.getUserId());
        vo.setProviderNo(provider.getProviderNo());
        vo.setRealName(provider.getRealName());
        vo.setPhone(provider.getPhone());
        vo.setIdCardNo(provider.getIdCardNo());
        vo.setServiceTypes(provider.getServiceTypes());
        vo.setServiceTypeList(splitServiceTypes(provider.getServiceTypes()));
        vo.setCityCode(provider.getCityCode());
        vo.setVehiclePlate(provider.getVehiclePlate());
        vo.setVehicleModel(provider.getVehicleModel());
        vo.setDrivingYears(provider.getDrivingYears());
        vo.setRating(provider.getRating());
        vo.setOrderCount(provider.getOrderCount());
        vo.setOnlineStatus(provider.getOnlineStatus());
        vo.setAcceptEnabled(provider.getAcceptEnabled());
        vo.setAuditStatus(provider.getAuditStatus());
        vo.setStatus(provider.getStatus());
        vo.setRemark(provider.getRemark());
        vo.setCreateTime(provider.getCreateTime());

        if (provider.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(provider.getUserId());
            vo.setUsername(user == null ? null : user.getUsername());
        }
        return vo;
    }

    // 分割可服务类型
    private List<String> splitServiceTypes(String serviceTypes) {
        if (!StringUtils.hasText(serviceTypes)) {
            return Collections.emptyList();
        }
        return Arrays.stream(serviceTypes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
