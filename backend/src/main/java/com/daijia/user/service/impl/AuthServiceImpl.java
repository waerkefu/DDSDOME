package com.daijia.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;
import com.daijia.security.JwtUtil;
import com.daijia.security.PasswordEncoder;
import com.daijia.user.dto.CurrentUserVO;
import com.daijia.user.dto.LoginRequest;
import com.daijia.user.dto.LoginVO;
import com.daijia.user.dto.RegisterRequest;
import com.daijia.user.dto.UserVO;
import com.daijia.user.entity.SysRole;
import com.daijia.user.entity.SysUser;
import com.daijia.user.entity.SysUserRole;
import com.daijia.user.enums.UserType;
import com.daijia.user.mapper.SysPermissionMapper;
import com.daijia.user.mapper.SysRoleMapper;
import com.daijia.user.mapper.SysUserMapper;
import com.daijia.user.mapper.SysUserRoleMapper;
import com.daijia.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 注册用户的默认角色编码 */
    private static final String DEFAULT_ROLE_CODE = "CUSTOMER";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final JwtUtil jwtUtil;

    // 登录逻辑
    @Override
    public LoginVO login(LoginRequest request) {
        String account = request.getUsername();
        // 校验账号是否存在
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.eq(SysUser::getUsername, account).or().eq(SysUser::getPhone, account)));
        SysUser user = users.isEmpty() ? null : users.get(0);

        // 校验密码是否正确
        if (user == null || !PasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.ACCOUNT_PASSWORD_ERROR);
        }
        
        // 校验账号是否已禁用
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        // 更新用户登录时间
        LocalDateTime now = LocalDateTime.now();//获取当前时间戳
        //创建user对象准备存入数据库
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(now);
        //更新数据库登录时间
        sysUserMapper.updateById(update);
        user.setLastLoginTime(now);

        return buildLoginVO(user);
    }
    
    // 注册
    @Override
    public LoginVO register(RegisterRequest request) {
        // 校验账号或手机号是否已存在
        Long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.eq(SysUser::getUsername, request.getUsername())
                        .or().eq(SysUser::getPhone, request.getPhone())));
        // 校验账号或手机号是否已存在
        if (exists != null && exists > 0) {
            // 账号或手机号已存在
            // 抛出异常
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }

        // 校验默认客户角色是否存在
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, DEFAULT_ROLE_CODE)
                .last("LIMIT 1"));
        // 校验默认客户角色是否存在
        // 抛出异常
        if (role == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "默认客户角色未配置");
        }

        // 注册用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setUserType(UserType.CUSTOMER.getCode());
        user.setStatus(1);
        //存入数据库
        sysUserMapper.insert(user);

        // 注册用户角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        //存入数据库
        sysUserRoleMapper.insert(userRole);

        return buildLoginVO(user);
    }

    // 查询当前用户信息
    @Override
    public CurrentUserVO currentUser(Long userId) {
        // 校验用户是否存在
        SysUser user = sysUserMapper.selectById(userId);
        //如果用户不存在，抛出异常
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        // 取数据往构建当前用户VO对象里填充
        CurrentUserVO vo = new CurrentUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setUserType(user.getUserType());
        vo.setRoles(sysRoleMapper.selectRoleCodesByUserId(userId));
        vo.setPermissions(sysPermissionMapper.selectPermCodesByUserId(userId));
        return vo;
    }

    // 构建登录 VO 回传给前端。为Login类的打工
    private LoginVO buildLoginVO(SysUser user) {
        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(user.getId());

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setPhone(user.getPhone());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setUserType(user.getUserType());
        UserType userType = UserType.of(user.getUserType());
        userVO.setUserTypeDesc(userType == null ? null : userType.getDescription());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setRoles(roles == null ? new ArrayList<>() : roles);

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType()));
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtUtil.getExpireSeconds());
        vo.setUser(userVO);
        return vo;
    }
}
