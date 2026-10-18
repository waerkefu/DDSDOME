package com.daijia.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页结果
 * 分页 出参 DTO
 * 被 Result 包装返回
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records = Collections.emptyList();// 分页记录列表
    private long total;// 总记录数
    private long current;// 当前页码
    private long size;// 每页条数

    /** 创建分页结果 */
    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records == null ? Collections.emptyList() : records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }

    /** 创建空分页结果 */
    public static <T> PageResult<T> empty(long current, long size) {
        // 如果当前页码或每页条数为空或小于 1，返回空分页结果
        // 否则，返回包含空记录列表的分页结果
        return of(Collections.emptyList(), 0L, current, size);
    }
}
