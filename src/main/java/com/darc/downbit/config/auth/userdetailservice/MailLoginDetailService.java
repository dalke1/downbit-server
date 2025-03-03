package com.darc.downbit.config.auth.userdetailservice;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/2-01:21:54
 * @description
 */
public class MailLoginDetailService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
