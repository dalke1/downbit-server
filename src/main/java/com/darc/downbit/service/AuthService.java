package com.darc.downbit.service;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.LoginDto;
import com.darc.downbit.common.dto.rep.PhoneLoginDto;
import com.darc.downbit.common.dto.rep.RegisterDto;
import com.darc.downbit.common.dto.resp.PhoneLoginResp;

import java.awt.image.BufferedImage;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-4:11:03
 * @description
 */
public interface AuthService {
    String loginByUsername(String captchaKey, LoginDto loginDto);

    PhoneLoginResp loginByPhone(PhoneLoginDto loginDto, String captchaKey);

    BufferedImage getCaptcha(String key);

    RestResp<String> register(String captchaKey, RegisterDto registerDto);

    RestResp<String> logout();

    RestResp<String> refreshToken();

    void sendSmsCode(String phone);
}
