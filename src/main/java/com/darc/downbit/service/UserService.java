package com.darc.downbit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.darc.downbit.common.dto.resp.ImgRespDto;
import com.darc.downbit.dao.entity.User;

import java.util.List;

/**
 * @author darc
 * @description 针对表【user】的数据库操作Service
 * @createDate 2024-07-22 05:13:45
 */
public interface UserService extends IService<User> {

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    List<User> listAll();

    /**
     * 添加用户
     *
     * @return 添加结果
     */
    boolean addUser(User user);

    /**
     * 获取用户头像
     *
     * @return 用户头像
     */
    ImgRespDto getAvatar();

}
