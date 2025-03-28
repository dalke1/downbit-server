package com.darc.downbit.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 视频表
 *
 * @author darc
 * @TableName video
 */
@TableName(value = "video")
@Data
public class Video implements Serializable {
    /**
     * 视频id
     */
    @TableId(type = IdType.AUTO)
    private Integer videoId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 上传文件id
     */
    private Integer fileId;

    /**
     * 视频标题
     */
    private String videoTitle;

    /**
     * 视频封面文件id
     */
    private Integer coverFileId;

    /**
     * 视频简介
     */
    private String videoDescription;

    /**
     * 视频时长
     */
    private Integer duration;

    /**
     * 视频格式
     */

    private String videoFormat;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 观看数
     */
    private Integer watchCount;


    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 发布时间
     */
    private Date uploadTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Video video = (Video) o;
        return Objects.equals(videoId, video.videoId) && Objects.equals(userId, video.userId) && Objects.equals(fileId, video.fileId) && Objects.equals(videoTitle, video.videoTitle) && Objects.equals(coverFileId, video.coverFileId) && Objects.equals(videoDescription, video.videoDescription) && Objects.equals(duration, video.duration) && Objects.equals(videoFormat, video.videoFormat) && Objects.equals(likeCount, video.likeCount) && Objects.equals(commentCount, video.commentCount) && Objects.equals(watchCount, video.watchCount) && Objects.equals(favoriteCount, video.favoriteCount) && Objects.equals(uploadTime, video.uploadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId, userId, fileId, videoTitle, coverFileId, videoDescription, duration, videoFormat, likeCount, commentCount, watchCount, favoriteCount, uploadTime);
    }

    @Override
    public String toString() {
        return "Video{" +
                "videoId=" + videoId +
                ", userId=" + userId +
                ", fileId=" + fileId +
                ", videoTitle='" + videoTitle + '\'' +
                ", coverFileId=" + coverFileId +
                ", videoDescription='" + videoDescription + '\'' +
                ", duration=" + duration +
                ", videoFormat='" + videoFormat + '\'' +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", watchCount=" + watchCount +
                ", favoriteCount=" + favoriteCount +
                ", uploadTime=" + uploadTime +
                '}';
    }
}