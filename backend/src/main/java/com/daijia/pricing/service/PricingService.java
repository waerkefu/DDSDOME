package com.daijia.pricing.service;

import com.daijia.common.PageResult;
import com.daijia.order.enums.OrderType;
import com.daijia.pricing.dto.PriceEstimateRequest;
import com.daijia.pricing.dto.PriceEstimateVO;
import com.daijia.pricing.dto.PricingRuleQuery;
import com.daijia.pricing.dto.PricingRuleSaveRequest;
import com.daijia.pricing.dto.PricingRuleVO;

import java.time.LocalDateTime;

/**
 * 计价服务：预估与最终结算共用同一套算法。
 */
public interface PricingService {

    /**
     * 价格预估
     */
    PriceEstimateVO estimate(PriceEstimateRequest req);

    /**
     * 计算订单金额（分），供下单预估与结束服务结算复用。
     *
     * @param serviceTime 用于判定夜间加价的计费时间，为空时取当前时间
     */
    int calculateAmount(String cityCode, String vehicleType, OrderType orderType,
                        int distanceM, int durationS, LocalDateTime serviceTime);

    /** 分页查询计价规则 */
    PageResult<PricingRuleVO> page(PricingRuleQuery query);

    /** 创建计价规则 */
    Long create(PricingRuleSaveRequest req);

    /** 更新计价规则 */
    void update(Long id, PricingRuleSaveRequest req);

    /** 删除计价规则（逻辑删除） */
    void delete(Long id);
}
