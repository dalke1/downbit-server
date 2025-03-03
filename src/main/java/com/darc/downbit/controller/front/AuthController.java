package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.LoginDto;
import com.darc.downbit.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/username_login")
    public Object usernameLogin(@RequestBody @Validated LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        String captchaKey = request.getHeader("captcha-key");
        String loginKey = request.getHeader("login-key");
        if (captchaKey == null && loginKey == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return RestResp.badRequest("验证码key不能为空");
        }
        return authService.loginByUsername(captchaKey, loginKey, loginDto);
    }

    @PostMapping("/phone_login")
    public Object phoneLogin() {
        return null;
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
    public Object register(@RequestBody @Validated LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        String captchaKey = request.getHeader("captcha-key");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        if (captchaKey == null) {
            return RestResp.badRequest("验证码key不能为空");
        }
        return authService.register(captchaKey, loginDto);
    }

    @PostMapping("/logout")
    public Object logout() {
        return authService.logout();
    }

    @PostMapping("/refresh_token")
    public Object refreshToken() {
        return null;
    }
}
