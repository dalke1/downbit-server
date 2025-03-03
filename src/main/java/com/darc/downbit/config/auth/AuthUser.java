package com.darc.downbit.config.auth;

import com.darc.downbit.dao.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/27-23:32:54
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthUser implements UserDetails {
    private User user;
    private Boolean isGuest = false;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
