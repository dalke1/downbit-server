package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.FavoriteVideo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 16216
 * @description 针对表【favorite_video(收藏夹视频关系表)】的数据库操作Mapper
 * @createDate 2024-12-26 23:59:02
 * @Entity com.darc.downbit.dao.entity.FavoriteVideo
 */
public interface FavoriteVideoMapper extends BaseMapper<FavoriteVideo> {
    @Insert("insert into favorite_video (favorite_id, video_id) values (" +
            "(select favorite.favorite_id from favorite where user_id = #{userId} and favorite_name = #{favoriteName})," +
            "#{videoId})")
    int insertByFavoriteNameAndVideoTitle(Integer userId, String favoriteName, Integer videoId);


    @Delete("delete from favorite_video where favorite_id = " +
            "(select favorite.favorite_id from favorite where user_id = #{userId} and favorite_name = #{favoriteName})" +
            "and video_id = #{videoId}")
    int deleteByFavoriteNameAndVideoTitle(Integer userId, String favoriteName, Integer videoId);


    @Select("select video_id from favorite_video where favorite_id = " +
            "(select favorite.favorite_id from favorite where user_id = #{userId} and favorite_name = #{favoriteName})")
    List<Integer> getVideosIdByFavoriteName(Integer userId, String favoriteName);

    /**
     * 先根据用户id查询所有的收藏夹id，再根据收藏夹id查询所有的视频id,看是否有该视频id
     */
    @Select("select count(*) from favorite_video where favorite_id in " +
            "(select favorite_id from favorite where user_id = #{userId}) and video_id = #{videoId}")
    int isVideoInFavorite(Integer userId, Integer videoId);


    /**
     * 根据视频标题和用户id查看该视频是否被收藏
     */
    @Select("select count(*) from favorite_video where video_id = " +
            "(select video_id from video where video_title = #{videoTitle}) and favorite_id in " +
            "(select favorite_id from favorite where user_id = #{userId})")
    int isVideoInFavoriteByVideoTitle(Integer userId, String videoTitle);

    /**
     * 根据用户id获取该用户收藏的所有视频标题
     */
    @Select("select video_title from video where video_id in " +
            "(select video_id from favorite_video where favorite_id in " +
            "(select favorite_id from favorite where user_id = #{userId}))")
    List<String> getAllFavoriteVideoTitlesByUserId(Integer userId);
}




