package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.Img;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author darc
 * @description 针对表【img(图片表)】的数据库操作Mapper
 * @createDate 2024-12-19 00:18:20
 * @Entity com.darc.downbit.dao.entity.Img
 */
public interface ImgMapper extends BaseMapper<Img> {
    @Select("select file.file_name from file" +
            "    left join img on file.file_id = img.file_id" +
            "    where img.user_id = #{userId} and file.file_type = 'avatar'")
    String getAvatarByUserId(Integer userId);


    @Select("select file.file_name from file" +
            "    left join img on file.file_id = img.file_id" +
            "    where img.user_id = (select user_id from user where username = #{username}) and file.file_type = 'avatar'")
    String getAvatarByUsername(String username);

    @Select("select file.file_name from file" +
            "    left join img on file.file_id = img.file_id" +
            "    where img.user_id = #{userId} and file.file_type = 'cover'")
    List<String> getVideoCoversByUserId(Integer userId);
}




