package com.darc.downbit.config.auth;

import com.darc.downbit.config.auth.filter.JwtFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/19-16:58:26
 * @description
 */
@Slf4j
@SpringBootConfiguration
public class AuthConfig {

    @Resource
    JwtFilter jwtFilter;

    @Resource
    CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/**").permitAll()
                                .anyRequest().authenticated()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, AuthorizationFilter.class);
//                .exceptionHandling(
//                        exception -> {
//                            exception.accessDeniedHandler((request, response, accessDeniedException) ->
//                                    ResponseUtil.sendForbidden(request, response, accessDeniedException.getMessage())
//                            );
//                            exception.authenticationEntryPoint((request, response, authException) ->
//                                    ResponseUtil.sendUnauthorized(request, response, authException.getMessage())
//                            );
//                        }
//                );

        return http.build();
    }

    /**
     * 自定义用户名密码登录认证管理器,可以在authService中使用,用于用户名密码登录
     *
     * @param normalLogin     自定义的使用用户名密码登录的UserDetailsService
     * @param passwordEncoder 密码编码器
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager normalAuthenticationManager(@Qualifier("normalLogin") UserDetailsService normalLogin, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(normalLogin);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * 自定义用户名密码登录的UserDetailsService
     *
     * @return UserDetailsService
     */
    @Bean
    public UserDetailsService normalLogin() {
        return new NormalLoginDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static AuthUser getAuthUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUser authUser) {
            return authUser;
        } else {
            return null;
        }
    }
}
