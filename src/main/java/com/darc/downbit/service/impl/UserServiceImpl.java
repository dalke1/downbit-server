package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.common.dto.resp.ImgRespDto;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.mapper.ImgMapper;
import com.darc.downbit.dao.mapper.UserMapper;
import com.darc.downbit.service.UserService;
import com.darc.downbit.util.CosUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author darc
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-07-22 05:13:45
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ImgMapper imgMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private CosUtil cosUtil;

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    @Override
    public boolean addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user) > 0;
    }

    @Override
    public ImgRespDto getAvatar() {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();
        String username = user.getUsername();
        return new ImgRespDto(cosUtil.getAvatarUrl(username));
    }

}




