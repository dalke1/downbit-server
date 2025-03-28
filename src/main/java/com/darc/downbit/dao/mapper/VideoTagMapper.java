package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.VideoTag;
import org.apache.ibatis.annotations.Delete;

/**
 * @author 16216
 * @description 针对表【video_tag(视频标签关联表)】的数据库操作Mapper
 * @createDate 2025-03-05 18:59:47
 * @Entity com.darc.downbit.dao.entity.VideoTag
 */
public interface VideoTagMapper extends BaseMapper<VideoTag> {
    @Delete("delete from video_tag where video_id = #{videoId}")
    int deleteVideoTagByVideoId(Integer videoId);
}




