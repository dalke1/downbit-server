package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/6-19:25:30
 * @description
 */
@Data
public class RegisterDto {
    @NotNull(message = "用户名不能为空")
    @NotBlank(message = "用户名不能为空格")
    private String username;
    @NotNull(message = "昵称不能为空")
    @NotBlank(message = "昵称不能为空格")
    private String nickname;
    @NotNull(message = "手机号不能为空")
    @NotBlank(message = "手机号不能为空格")
    private String phone;
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
