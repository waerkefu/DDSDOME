package com.daijia.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.provider.entity.Provider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProviderMapper extends BaseMapper<Provider> {

    /**
     * 按登录用户查询服务商档案
     */
    @Select("SELECT * FROM provider WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    Provider selectByUserId(@Param("userId") Long userId);
}
