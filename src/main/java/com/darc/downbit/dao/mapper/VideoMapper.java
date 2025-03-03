package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.po.CoverPo;
import com.darc.downbit.common.po.TagPo;
import com.darc.downbit.common.po.VideoPo;
import com.darc.downbit.dao.entity.Video;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author darc
 * @description 针对表【video(视频表)】的数据库操作Mapper
 * @createDate 2024-12-19 00:18:38
 * @Entity com.darc.downbit.dao.entity.Video
 */
public interface VideoMapper extends BaseMapper<Video> {

    @Select("select user.username as uploader,file.file_name,video.video_title from file" +
            "    left join video on file.file_id = video.file_id" +
            "    left join user on user.user_id = video.user_id" +
            "    where video.video_id = #{videoId}")
    VideoPo getVideoByVideoId(Integer videoId);

    @Select("select video.video_id,user.username as uploader,file.file_name,video.video_title,video.upload_time from file" +
            "    left join video on file.file_id = video.file_id" +
            "    left join user on user.user_id = video.user_id" +
            "    where video.video_id = #{videoId}")
    VideoCache getVideoCacheByVideoId(Integer videoId);

    @Select("select user.username as uploader,file.file_name,video.video_title from file" +
            "    left join video on file.file_id = video.file_id" +
            "    left join user on user.user_id = video.user_id" +
            "    where video.video_title = #{videoTitle}")
    VideoPo getVideoByVideoTitle(String videoTitle);

    @Select("select file.file_name as coverFileName from file" +
            "    left join video on file.file_id = video.cover_file_id" +
            "    where video.video_id = #{videoId}")
    String getCoverByVideoId(Integer videoId);

    @Select("select user.username as uploader,file.file_name,video.video_title from file" +
            "    left join video on file.file_id = video.file_id" +
            "    left join user on user.user_id = video.user_id" +
            "    where video.user_id = #{userId} and file.file_type = 'video'" +
            "    limit 1")
    List<VideoPo> getVideoByUserId(Integer userId);

    @Select("select user.username as uploader,file.file_name,video.video_title from file" +
            "    left join video on file.file_id = video.file_id" +
            "    left join user on user.user_id = video.user_id" +
            "    where video.user_id = #{userId} and file.file_type = 'video'")
    List<VideoPo> getVideosByUserId(Integer userId);


    @Select("select video_id from video where user_id = #{userId}")
    List<Integer> getVideosIdByUserId(Integer userId);

    @Select("select video_id from video where video_title = #{videoTitle}")
    Integer getVideoIdByVideoTitle(String videoTitle);


    List<VideoPo> getVideosByVideoTitles(@Param("titles") List<String> videoTitles);

    List<String> getVideoFilesByVideoTitles(@Param("titles") List<String> videoTitles);

    List<String> getCoverFilesByVideoTitles(@Param("titles") List<String> videoTitles);

    List<CoverPo> getCoversAndTitlesByVideosId(@Param("videoIds") List<Integer> videoIds);

    List<CoverPo> getCoversAndTitlesByVideosTitle(@Param("titles") List<String> videoTitles);


    @Select("select tag.tag_name from video_tag" +
            "    left join tag on video_tag.tag_id = tag.tag_id" +
            "    where video_tag.video_id = #{videoId}")
    List<String> getTagsByVideoId(Integer videoId);

    @Select("select video.video_title,tag_name from video " +
            "     left join video_tag on video.video_id = video_tag.video_id" +
            "     left join tag on video_tag.tag_id = tag.tag_id")
    List<TagPo> getAllVideoTitlesAndTags();


    /**
     * 获取所有未观看视频的标题和标签
     *
     * @param watchedVideoTitles 已观看视频标题
     * @return List<TagPo>
     */
    List<TagPo> getUnwatchedVideoTitlesAndTags(@Param("watchedTitles") List<String> watchedVideoTitles,
                                               @Param("limit") Integer limit);

    List<Integer> getVideosIdInTagsSortedByUploadTime(@Param("tags") List<String> tags,
                                                      @Param("limit") Integer limit);


    @Select("select file.file_name from file" +
            "    left join video on file.file_id = video.file_id" +
            "    where video.video_title = #{videoTitle}")
    String getFileNameByVideoTitle(String videoTitle);

    @Select("select file.file_name from file" +
            "    left join video on file.file_id = video.cover_file_id" +
            "    where video.video_title = #{videoTitle}")
    String getCoverByVideoTitle(String videoTitle);

    @Select("select tag.tag_name from video_tag" +
            "    left join tag on video_tag.tag_id = tag.tag_id" +
            "    where video_tag.video_id = (select video_id from video where video_title = #{videoTitle})")
    List<String> getTagsByVideoTitle(String videoTitle);

    @Select("select video.video_id from video where video.user_id = #{userId} order by upload_time desc limit #{limit}")
    List<Integer> getVideosIdByUserIdSortByUploadTime(Integer userId, Integer limit);
}




