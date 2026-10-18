package com.daijia.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基础参数
 * 用于所有分页查询的参数，确保分页参数的合法性。
 * 分页 入参 DTO
 * 被 Controller 接收
 */
@Data
//继承 Serializable 接口，确保可以序列化
public class PageQuery implements Serializable {

    /** 最大每页条数 */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;
    /** 每页条数 */
    private Long pageSize = 10L;

    /** 安全的页码，确保在有效范围内 */
    public long safePageNum() {
        // 如果页码为空或小于 1，返回 1
        // 否则，返回原始页码值
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    /** 安全的每页条数，确保在有效范围内 */
    public long safePageSize() {
        // 如果每页条数为空或小于 1，返回 10
        // 否则，返回原始每页条数值
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
