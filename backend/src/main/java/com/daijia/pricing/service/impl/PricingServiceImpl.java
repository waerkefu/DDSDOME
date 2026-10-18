package com.daijia.pricing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.daijia.common.BusinessException;
import com.daijia.common.PageResult;
import com.daijia.common.ResultCode;
import com.daijia.order.enums.OrderType;
import com.daijia.pricing.dto.PriceEstimateRequest;
import com.daijia.pricing.dto.PriceEstimateVO;
import com.daijia.pricing.dto.PricingRuleQuery;
import com.daijia.pricing.dto.PricingRuleSaveRequest;
import com.daijia.pricing.dto.PricingRuleVO;
import com.daijia.pricing.entity.PricingRule;
import com.daijia.pricing.mapper.PricingRuleMapper;
import com.daijia.pricing.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 计价服务实现。
 *
 * <p>金额全程以「分」参与运算，所有中间结果向上取整，避免浮点误差。</p>
 */
@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PricingRuleMapper pricingRuleMapper;

    /** 计价
     * @param req 计价请求
     * @return 计价结果
     */
    @Override
    public PriceEstimateVO estimate(PriceEstimateRequest req) {
        // 加载计价规则
        PricingRule rule = loadRule(req.getCityCode(), req.getVehicleType());
        // serviceTime 为空时，默认当前时间计费
        LocalDateTime serviceTime = req.getBookingTime() != null ? req.getBookingTime() : LocalDateTime.now();
        return doCalculate(rule, req.getOrderType(),
                intValue(req.getDistanceM()), intValue(req.getDurationS()), serviceTime);
    }

    /** 计费
     * @param cityCode 城市编码
     * @param vehicleType 车辆类型
     * @param orderType 订单类型
     * @param distanceM 里程（米）
     * @param durationS 时长（秒）
     * @param serviceTime 计费时间
     * @return 最终金额（分）
     */
    @Override
    public int calculateAmount(String cityCode, String vehicleType, OrderType orderType,
                               int distanceM, int durationS, LocalDateTime serviceTime) {
        PricingRule rule = loadRule(cityCode, vehicleType);
        LocalDateTime time = serviceTime != null ? serviceTime : LocalDateTime.now();
        return doCalculate(rule, orderType, distanceM, durationS, time).getTotalAmount();
    }

    /**
     * 计费核心算法：
     * 1. 起步价已包含 baseDistanceM 米与 baseDurationS 秒；
     * 2. 超出部分分别按 里程单价、时长单价 计费并向上取整；
     * 3. 计费时间落在夜间区间时，对「里程费 + 时长费」乘夜间系数；
     * 4. 预约单再对总额乘预约系数；
     * 5. 最终不低于最低消费。
     * @param rule 计价规则
     * @param orderType 订单类型
     * @param distanceM 里程（米）
     * @param durationS 时长（秒）
     * @param serviceTime 计费时间
     * @return 计价结果
     */
    private PriceEstimateVO doCalculate(PricingRule rule, OrderType orderType,
                                        int distanceM, int durationS, LocalDateTime serviceTime) {
        int basePrice = intValue(rule.getBasePrice());
        int baseDistanceM = intValue(rule.getBaseDistanceM());
        int baseDurationS = intValue(rule.getBaseDurationS());

        int excessDistanceM = Math.max(0, distanceM - baseDistanceM);
        int excessDurationS = Math.max(0, durationS - baseDurationS);

        // 里程费 = ceil(超出米 / 1000 * 每公里单价)（分）
        int distanceFee = ceilDivide((long) excessDistanceM * intValue(rule.getPricePerKm()), 1000L);
        // 时长费 = ceil(超出秒 / 60 * 每分钟单价)（分）
        int durationFee = ceilDivide((long) excessDurationS * intValue(rule.getPricePerMin()), 60L);

        BigDecimal nightRatio = BigDecimal.ONE;
        if (isNight(serviceTime.toLocalTime(), rule.getNightStart(), rule.getNightEnd())
                && rule.getNightRatio() != null) {
            nightRatio = rule.getNightRatio();
        }
        int variableFee = ceilRatio(distanceFee + durationFee, nightRatio);

        BigDecimal bookingRatio = BigDecimal.ONE;
        if (orderType == OrderType.SCHEDULED && rule.getBookingRatio() != null) {
            bookingRatio = rule.getBookingRatio();
        }
        int total = ceilRatio(basePrice + variableFee, bookingRatio);
        total = Math.max(total, intValue(rule.getMinPrice()));

        PriceEstimateVO vo = new PriceEstimateVO();
        vo.setTotalAmount(total);
        vo.setBasePrice(basePrice);
        vo.setDistanceFee(distanceFee);
        vo.setDurationFee(durationFee);
        vo.setNightRatio(nightRatio);
        vo.setBookingRatio(bookingRatio);
        vo.setMinPrice(intValue(rule.getMinPrice()));
        return vo;
    }

    /** 判断时间是否落在夜间区间，支持跨零点（如 22:00 ~ 06:00） 
     * @param time 时间
     * @param start 开始时间
     * @param end 结束时间
     * @return 是否落在夜间区间
    */
    private boolean isNight(LocalTime time, LocalTime start, LocalTime end) {
        if (time == null || start == null || end == null || start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        return !time.isBefore(start) || time.isBefore(end);
    }

    /** 非负整数除法向上取整 
     * @param numerator 分子
     * @param denominator 分母
     * @return 向上取整结果
    */
    private int ceilDivide(long numerator, long denominator) {
        if (numerator <= 0) {
            return 0;
        }
        return (int) ((numerator + denominator - 1) / denominator);
    }

    /** 金额乘系数后向上取整为分
     * @param amount 金额（分）
     * @param ratio 系数
     * @return 向上取整结果（分）
     */
    private int ceilRatio(int amount, BigDecimal ratio) {
        if (amount <= 0 || ratio == null || ratio.compareTo(BigDecimal.ONE) == 0) {
            return amount;
        }
        return ratio.multiply(BigDecimal.valueOf(amount)).setScale(0, RoundingMode.CEILING).intValue();
    }

    /** 加载计价规则
     * @param cityCode 城市编码
     * @param vehicleType 车型
     * @return 计价规则
     */
    private PricingRule loadRule(String cityCode, String vehicleType) {
        PricingRule rule = pricingRuleMapper.selectEffectiveRule(cityCode, vehicleType);
        if (rule == null) {
            throw new BusinessException(ResultCode.PRICING_RULE_NOT_FOUND,
                    String.format("未找到城市[%s]车型[%s]的计价规则", cityCode, vehicleType));
        }
        return rule;
    }

    /** 整数转换为非负整数
     * @param value 整数
     * @return 非负整数
     */
    private int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    /** 分页查询计价规则
     * @param query 查询参数
     * @return 分页结果
     * */
    @Override
    public PageResult<PricingRuleVO> page(PricingRuleQuery query) {
        Page<PricingRule> page = new Page<>(query.safePageNum(), query.safePageSize());
        Page<PricingRule> result = pricingRuleMapper.selectPage(page, Wrappers.<PricingRule>lambdaQuery()
                .eq(StringUtils.hasText(query.getCityCode()), PricingRule::getCityCode, query.getCityCode())
                .eq(StringUtils.hasText(query.getVehicleType()), PricingRule::getVehicleType, query.getVehicleType())
                .orderByDesc(PricingRule::getId));
        List<PricingRuleVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 创建计价规则
     * @param req 创建请求
     * @return 计价规则ID
     * */
    @Override
    //方法开始前开启事务，正常结束则提交，抛异常则回滚
    @Transactional(rollbackFor = Exception.class)
    public Long create(PricingRuleSaveRequest req) {
        assertNotExists(req.getCityCode(), req.getVehicleType(), null);
        PricingRule rule = new PricingRule();
        apply(rule, req);
        try {
            pricingRuleMapper.insert(rule);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PRICING_RULE_EXISTS);
        }
        return rule.getId();
    }

    /** 更新计价规则
     * @param id 计价规则ID
     * @param req 更新请求
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PricingRuleSaveRequest req) {
        PricingRule exists = pricingRuleMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException(ResultCode.PRICING_RULE_NOT_FOUND);
        }
        assertNotExists(req.getCityCode(), req.getVehicleType(), id);
        PricingRule rule = new PricingRule();
        rule.setId(id);
        apply(rule, req);
        try {
            pricingRuleMapper.updateById(rule);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PRICING_RULE_EXISTS);
        }
    }

    /** 删除计价规则（逻辑删除） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查是否存在
        PricingRule exists = pricingRuleMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException(ResultCode.PRICING_RULE_NOT_FOUND);
        }
        // 逻辑删除
        pricingRuleMapper.deleteById(id);
    }

    /** 检查计价规则是否存在 */
    private void assertNotExists(String cityCode, String vehicleType, Long excludeId) {
        Long count = pricingRuleMapper.selectCount(Wrappers.<PricingRule>lambdaQuery()
                .eq(PricingRule::getCityCode, cityCode)
                .eq(PricingRule::getVehicleType, vehicleType)
                .ne(excludeId != null, PricingRule::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.PRICING_RULE_EXISTS);
        }
    }

    /** 应用计价规则 */
       private void apply(PricingRule rule, PricingRuleSaveRequest req) {
        rule.setRuleName(req.getRuleName());
        rule.setCityCode(req.getCityCode());
        rule.setCityName(req.getCityName());
        rule.setVehicleType(req.getVehicleType());
        rule.setBasePrice(req.getBasePrice());
        rule.setBaseDistanceM(req.getBaseDistanceM() == null ? 0 : req.getBaseDistanceM());
        rule.setBaseDurationS(req.getBaseDurationS() == null ? 0 : req.getBaseDurationS());
        rule.setPricePerKm(req.getPricePerKm() == null ? 0 : req.getPricePerKm());
        rule.setPricePerMin(req.getPricePerMin() == null ? 0 : req.getPricePerMin());
        rule.setMinPrice(req.getMinPrice() == null ? 0 : req.getMinPrice());
        rule.setNightStart(req.getNightStart());
        rule.setNightEnd(req.getNightEnd());
        rule.setNightRatio(req.getNightRatio() == null ? BigDecimal.ONE : req.getNightRatio());
        rule.setBookingRatio(req.getBookingRatio() == null ? BigDecimal.ONE : req.getBookingRatio());
        rule.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    }

    /** 转换为VO */
    private PricingRuleVO toVO(PricingRule rule) {
        PricingRuleVO vo = new PricingRuleVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setCityCode(rule.getCityCode());
        vo.setCityName(rule.getCityName());
        vo.setVehicleType(rule.getVehicleType());
        vo.setBasePrice(rule.getBasePrice());
        vo.setBaseDistanceM(rule.getBaseDistanceM());
        vo.setBaseDurationS(rule.getBaseDurationS());
        vo.setPricePerKm(rule.getPricePerKm());
        vo.setPricePerMin(rule.getPricePerMin());
        vo.setMinPrice(rule.getMinPrice());
        vo.setNightStart(rule.getNightStart());
        vo.setNightEnd(rule.getNightEnd());
        vo.setNightRatio(rule.getNightRatio());
        vo.setBookingRatio(rule.getBookingRatio());
        vo.setStatus(rule.getStatus());
        vo.setCreateTime(rule.getCreateTime());
        return vo;
    }
}
