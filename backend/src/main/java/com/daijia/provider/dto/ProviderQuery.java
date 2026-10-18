package com.daijia.provider.dto;

import com.daijia.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务商分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderQuery extends PageQuery {

    /** 姓名 / 手机号 / 编号 模糊匹配 */
    private String keyword;

    /** 1=启用 0=禁用 */
    private Integer status;

    /** 0=待审核 1=通过 2=驳回 */
    private Integer auditStatus;

    private String cityCode;
}
