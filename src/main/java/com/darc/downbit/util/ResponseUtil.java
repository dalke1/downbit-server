package com.darc.downbit.util;

import com.darc.downbit.common.dto.RestResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/1-23:13:09
 * @description
 */
@Slf4j
public class ResponseUtil {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static void sendUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        log.error("[认证失败]{},来自ip:{}", message, request.getRemoteAddr());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(RestResp.unauthorized(message)));
        response.getWriter().flush();
        response.getWriter().close();
    }

    public static void sendForbidden(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        log.error("[拒绝访问]{},来自ip:{}", message, request.getRemoteAddr());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(RestResp.forbidden(message)));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
