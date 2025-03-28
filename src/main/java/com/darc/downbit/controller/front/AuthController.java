package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.LoginDto;
import com.darc.downbit.common.dto.rep.PhoneLoginDto;
import com.darc.downbit.common.dto.rep.RegisterDto;
import com.darc.downbit.common.exception.BadRequestException;
import com.darc.downbit.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-4:21:48
 * @description
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/username_login")
    public Object usernameLogin(@RequestBody @Validated LoginDto loginDto, HttpServletRequest request) {
        String captchaKey = request.getHeader("captcha-key");
        log.info("进入了controller层");
        if (captchaKey == null) {
            throw new BadRequestException("验证码key不能为空");
        }
        return RestResp.ok(authService.loginByUsername(captchaKey, loginDto));
    }

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response) {
        String uuid = UUID.randomUUID().toString();
        BufferedImage captcha = authService.getCaptcha(uuid);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("captcha-key", uuid);
        response.setHeader("Access-Control-Expose-Headers", "captcha-key");
        response.setContentType("image/jpeg");
        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(captcha, "jpg", out);
            out.flush();
        } catch (IOException e) {
            System.out.println("验证码输出失败");
        }
    }

    @PostMapping("/register")
    public Object register(@RequestBody @Validated RegisterDto registerDto, HttpServletRequest request, HttpServletResponse response) {
        String captchaKey = request.getHeader("captcha-key");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        if (captchaKey == null) {
            return RestResp.badRequest("验证码key不能为空");
        }
        return authService.register(captchaKey, registerDto);
    }

    @PostMapping("/logout")
    public Object logout() {
        return authService.logout();
    }

    @PostMapping("/refresh_token")
    public Object refreshToken() {
        return null;
    }

    @GetMapping("/smsCode")
    public Object smsCode(@RequestParam("phone") String phone) {
        authService.sendSmsCode(phone);
        return RestResp.ok();
    }

    @PostMapping("/phone_login")
    public Object phoneLogin(@RequestBody @Validated PhoneLoginDto phoneLoginDto, HttpServletRequest request) {
        String captchaKey = request.getHeader("captcha-key");
        if (captchaKey == null) {
            throw new BadRequestException("验证码key不能为空");
        }
        return RestResp.ok(authService.loginByPhone(phoneLoginDto, captchaKey));
    }
}
