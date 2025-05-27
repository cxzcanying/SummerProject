package com.flashsale.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * @author 21311
 */
@Data
public class PageResult<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 无参构造函数
     */
    public PageResult() {
    }
    
    /**
     * 全参构造函数
     */
    public PageResult(List<T> list, Long total, Integer page, Integer size) {
        this.list = list;
        this.total = total;
        this.current = page.longValue();
        this.size = size.longValue();
        this.pages = (long) Math.ceil(total / (double) size);
    }
} 