package com.daijia.pricing.dto;

import com.daijia.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计价规则分页查询条件
 */
@Data
/**
 * 让 Lombok 生成`equals` /`hashCode` 时
 * 把父类`PageQuery` 的`pageNum` 、`pageSize` 也纳入比较，
 * 保证「条件相同但页码不同」的两个查询对象不相等
 */
@EqualsAndHashCode(callSuper = true)
public class PricingRuleQuery extends PageQuery {
    /** 城市编码 */
    private String cityCode;
    /** 车辆类型 */
    private String vehicleType;
}
