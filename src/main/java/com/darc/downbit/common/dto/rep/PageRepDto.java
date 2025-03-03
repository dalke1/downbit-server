package com.darc.downbit.common.dto.rep;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/7/22-0:58:51
 * @description
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRepDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Boolean fetchAll = false;
}