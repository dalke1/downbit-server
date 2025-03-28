package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/28-21:28:16
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PhoneLoginResp {
    private String token;
    private String username;
}
