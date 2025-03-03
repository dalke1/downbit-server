package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.User;
import org.apache.ibatis.annotations.Select;

/**
 * @author darc
 * @description 针对表【user】的数据库操作Mapper
 * @createDate 2024-07-22 05:13:45
 * @Entity com.darc.entity.dao.downbit.User
 */
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT role.role_name FROM user " +
            "LEFT JOIN user_role ON user.user_id = user_role.user_id " +
            "LEFT JOIN role ON user_role.role_id = role.role_id " +
            "WHERE user.username = #{username}")
    String getRoleByUsername(String username);


    @Select("SELECT user.nickname FROM user WHERE user.username = #{username}")
    String findNicknameByUsername(String username);
}




