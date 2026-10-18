package com.daijia.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.user.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色映射接口
 * 映射数据库表sys_role表
 * DAO层接口，定义了对角色表的基本CRUD操作。使用MyBatis-Plus框架实现。
 * 单表操作由继承BaseMapper<SysRole>提供。
 * 复表操作（如查询用户拥有的角色编码）在SysRoleMapperImpl类中实现。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色编码
     */
    @Select("SELECT r.role_code FROM sys_role r "
            + "JOIN sys_user_role ur ON ur.role_id = r.id "
            + "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}