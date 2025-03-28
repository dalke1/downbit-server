package com.darc.downbit.config.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-1:20:26
 * @description
 */

public class NormalLoginDetailsService implements UserDetailsService {
    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<User> selectByUsername = new QueryWrapper<>();
        selectByUsername.eq("username", username);
        User user = userMapper.selectOne(selectByUsername);
        if (user == null) {
            throw new UsernameNotFoundException("找不到用户:" + username);
        }
        String role = userMapper.getRoleByUsername(username);
        user.setRole(role);
        AuthUser authUser = new AuthUser();
        authUser.setUser(user);
        return authUser;
    }
}
