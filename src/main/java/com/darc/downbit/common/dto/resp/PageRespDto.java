package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/7/22-1:10:54
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRespDto<T> {

    @JsonProperty("pageNum")
    private Integer pageNum;
    @JsonProperty("pageSize")
    private Integer pageSize;
    @JsonProperty("total")
    private Integer total;
    @JsonProperty("list")
    private List<? extends T> list;
    @JsonProperty("pages")
    private Integer pages;

    public static <T> PageRespDto<T> of(Integer pageNum, Integer pageSize, Integer total, List<T> list) {
        int pages = 0;
        if (pageSize != 0) {
            pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        }
        return new PageRespDto<>(pageNum, pageSize, total, list, pages);
    }
}
