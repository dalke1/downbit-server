package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.UserRole;

/**
 * @author darc
 * @description 针对表【user_role(用户角色关联表)】的数据库操作Mapper
 * @createDate 2024-12-03 01:49:24
 * @Entity com.darc.downbit.dao.entity.UserRole
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    int insertByUsernameAndRole(String username, int roleId);
}




