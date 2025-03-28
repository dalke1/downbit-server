package com.darc.downbit.common.dto.rep;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/29-01:43:56
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchVideoReq {
    // 搜索关键词
    private String query;
    // 当前页码
    private Integer page;
    // 排序方式
    private String sortBy;
    // 每页大小
    private Integer size;
}
