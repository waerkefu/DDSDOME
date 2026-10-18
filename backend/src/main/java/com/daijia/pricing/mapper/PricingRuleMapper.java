package com.daijia.pricing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.pricing.entity.PricingRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PricingRuleMapper extends BaseMapper<PricingRule> {

    /**
     * 查询「城市 + 车型」维度下启用中的计价规则
     */
    @Select("SELECT * FROM pricing_rule WHERE city_code = #{cityCode} AND vehicle_type = #{vehicleType} "
            + "AND status = 1 AND deleted = 0 ORDER BY id DESC LIMIT 1")
    PricingRule selectEffectiveRule(@Param("cityCode") String cityCode, @Param("vehicleType") String vehicleType);
}
