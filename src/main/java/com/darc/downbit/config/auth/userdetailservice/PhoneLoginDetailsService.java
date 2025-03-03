package com.darc.downbit.config.auth.userdetailservice;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-5:38:38
 * @description 通过查询redis数据库中存的手机号和验证码, 让手机号作为username, 验证码作为password来实现手机号登录的认证功能
 */
public class PhoneLoginDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
