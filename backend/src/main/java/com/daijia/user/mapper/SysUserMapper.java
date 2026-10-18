package com.daijia.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户映射接口
 * 映射数据库表sys_user表
 * DAO层接口，定义了对用户表的基本CRUD操作。使用MyBatis-Plus框架实现。
 * 单表操作由继承BaseMapper<SysUser>提供。
 * 复表操作（如查询用户拥有的角色编码）在SysUserMapperImpl类中实现。
 */
@Mapper
//继承 BaseMapper 接口，提供基本的数据库操作方法
public interface SysUserMapper extends BaseMapper<SysUser> {
}