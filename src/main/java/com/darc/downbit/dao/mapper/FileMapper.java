package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.File;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

/**
 * @author 16216
 * @description 针对表【file(上传到腾讯云cos的文件表)】的数据库操作Mapper
 * @createDate 2024-12-20 01:23:44
 * @Entity com.darc.downbit.dao.entity.File
 */
public interface FileMapper extends BaseMapper<File> {
    @Update("update file set file_name = #{coverFileName} " +
            "where file_id = (select cover_file_id from video where video_id = #{videoId})")
    int updateCoverFileName(Integer videoId, String coverFileName);

    @Update("update file set file_name = #{videoFileName} " +
            "where file_id = (select file_id from video where video_id = #{videoId})")
    int updateVideoFileName(Integer videoId, String videoFileName);

    @Delete("delete from file where file_id = #{fileId} ")
    int deleteVideoFile(Integer fileId);

    @Delete("delete from file where file_id = #{fileId} ")
    int deleteCoverFile(Integer fileId);
}




