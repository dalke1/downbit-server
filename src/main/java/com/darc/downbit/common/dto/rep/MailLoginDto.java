package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/2-01:23:04
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailLoginDto {
    @NotNull(message = "邮箱不能为空")
    @NotBlank(message = "邮箱不能为空格")
    private String mail;
    @NotNull(message = "密码不能为空")
    @NotBlank(message = "密码不能为空格")
    private String password;
    @NotNull(message = "验证码不能为空")
    @NotBlank(message = "验证码不能为空格")
    private String captcha;
}
