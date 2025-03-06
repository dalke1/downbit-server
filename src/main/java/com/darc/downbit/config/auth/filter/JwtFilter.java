package com.darc.downbit.config.auth.filter;

import com.darc.downbit.common.exception.BadTokenException;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/28-2:59:15
 * @description
 */
@Component
@EnableConfigurationProperties(FilterProperties.class)
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    @Resource
    RedisTemplate<String, User> redisTemplate;

    @Resource
    FilterProperties filterProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        List<String> passPath = filterProperties.getPassPath();
        String requestUri = request.getRequestURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String path : passPath) {
            if (pathMatcher.match(path, requestUri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        AuthUser authUser;
        String jwt = request.getHeader("Authorization");
        if (jwt != null) {
            if (!jwt.startsWith("token:")) {
                throw new BadTokenException("未携带令牌");
            }
            String token = jwt.substring(6);

            Claims claims = JwtUtil.parseToken(token);
            // 表示token无效
            if (claims == null) {
                throw new BadTokenException("无效的令牌");
            }

            authUser = validateUser(claims, ip, device);
            if (authUser == null) {
                throw new BadTokenException("令牌验证失败");
            }
        } else {
            List<String> guestPath = filterProperties.getGuestPath();
            for (String path : guestPath) {
                if (pathMatcher.match(path, requestUri)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            throw new BadTokenException("没有令牌");
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    /**
     * 验证token中的ip和device是否与当前ip和device一致
     *
     * @param claims        包含uuid,用户名或手机号
     * @param currentIp     请求的ip
     * @param currentDevice 请求的设备信息
     * @return boolean
     */
    private AuthUser validateUser(Claims claims, String currentIp, String currentDevice) {
        String uuid = claims.getSubject();
        String username = claims.get("username", String.class);

        // 验证redis中是否有该key
        if (Boolean.FALSE.equals(redisTemplate.hasKey("loginUser:" + username))) {
            return null;
        }

        User recordedUser = redisTemplate.opsForValue().get("loginUser:" + username);
        if (recordedUser == null) {
            return null;
        }

        if (!(Objects.equals(username, recordedUser.getUsername()) &&
                currentIp.equals(recordedUser.getIp()) &&
                currentDevice.equals(recordedUser.getDevice()) &&
                uuid.equals(recordedUser.getUuid()))) {
            return null;
        }
        // 如果token中的用户名,uuid和redis中存储一致,且登录的ip和device与redis中存储的一致,则验证通过
        AuthUser authUser = new AuthUser();
        authUser.setUser(recordedUser);
        return authUser;
    }
}
