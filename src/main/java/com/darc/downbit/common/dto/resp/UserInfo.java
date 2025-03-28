package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/28-12:26:08
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String username;
    private String nickname;
    private String intro;
    private String avatar;
}
