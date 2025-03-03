package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.Favorite;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 16216
 * @description 针对表【favorite】的数据库操作Mapper
 * @createDate 2024-12-26 23:58:40
 * @Entity com.darc.downbit.dao.entity.Favorite
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {
    @Select("SELECT favorite_name FROM favorite WHERE user_id = #{userId}")
    List<String> getFavoriteNamesByUserId(Integer userId);

}




