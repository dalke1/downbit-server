package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-4:36:32
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneLoginDto {
    @NotNull(message = "手机号不能为空")
    @NotBlank(message = "手机号不能为空格")
    private String phone;
    @NotNull(message = "验证码不能为空")
    @NotBlank(message = "验证码不能为空格")
    private String code;
    @NotNull(message = "验证码不能为空")
    @NotBlank(message = "验证码不能为空格")
    private String captcha;
}
