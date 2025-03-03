package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-4:02:49
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    @NotNull(message = "用户名不能为空")
    @NotBlank(message = "用户名不能为空格")
    private String username;
    @NotNull(message = "密码不能为空")
    @NotBlank(message = "密码不能为空格")
    private String password;
    @NotNull(message = "验证码不能为空")
    @NotBlank(message = "验证码不能为空格")
    private String captcha;
    @NotNull(message = "uuid不能为空")
    @NotBlank(message = "uuid不能为空格")
    private String uuid;
}
