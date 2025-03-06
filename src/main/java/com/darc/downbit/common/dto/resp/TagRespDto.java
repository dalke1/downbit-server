package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/6-01:25:52
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagRespDto {
    private String name;
    private String code;
}
