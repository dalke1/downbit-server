package com.darc.downbit.config.auth;


import com.darc.downbit.dao.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/1-21:22:45
 * @description 校验被请求接口需要的用户角色和登录用户的角色是否匹配
 */

@Component("authValidator")
public class AuthValidator {

    public boolean hasRole(String role) {
        User user = getCurrentUser();
        return user != null && user.getRole().equals(role);
    }

    public boolean hasAnyRole(String... roles) {
        User user = getCurrentUser();
        return user != null && Arrays.asList(roles).contains(user.getRole());
    }

    private User getCurrentUser() {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return authUser.getUser();
    }
}