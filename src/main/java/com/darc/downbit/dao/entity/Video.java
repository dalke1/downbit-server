package com.darc.downbit.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

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
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Video other = (Video) that;
        return (this.getVideoId() == null ? other.getVideoId() == null : this.getVideoId().equals(other.getVideoId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
                && (this.getVideoTitle() == null ? other.getVideoTitle() == null : this.getVideoTitle().equals(other.getVideoTitle()))
                && (this.getCoverFileId() == null ? other.getCoverFileId() == null : this.getCoverFileId().equals(other.getCoverFileId()))
                && (this.getVideoDescription() == null ? other.getVideoDescription() == null : this.getVideoDescription().equals(other.getVideoDescription()))
                && (this.getDuration() == null ? other.getDuration() == null : this.getDuration().equals(other.getDuration()))
                && (this.getLikeCount() == null ? other.getLikeCount() == null : this.getLikeCount().equals(other.getLikeCount()))
                && (this.getCommentCount() == null ? other.getCommentCount() == null : this.getCommentCount().equals(other.getCommentCount()))
                && (this.getWatchCount() == null ? other.getWatchCount() == null : this.getWatchCount().equals(other.getWatchCount()))
                && (this.getFavoriteCount() == null ? other.getFavoriteCount() == null : this.getFavoriteCount().equals(other.getFavoriteCount()))
                && (this.getUploadTime() == null ? other.getUploadTime() == null : this.getUploadTime().equals(other.getUploadTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getVideoId() == null) ? 0 : getVideoId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getVideoTitle() == null) ? 0 : getVideoTitle().hashCode());
        result = prime * result + ((getCoverFileId() == null) ? 0 : getCoverFileId().hashCode());
        result = prime * result + ((getVideoDescription() == null) ? 0 : getVideoDescription().hashCode());
        result = prime * result + ((getDuration() == null) ? 0 : getDuration().hashCode());
        result = prime * result + ((getLikeCount() == null) ? 0 : getLikeCount().hashCode());
        result = prime * result + ((getCommentCount() == null) ? 0 : getCommentCount().hashCode());
        result = prime * result + ((getWatchCount() == null) ? 0 : getWatchCount().hashCode());
        result = prime * result + ((getFavoriteCount() == null) ? 0 : getFavoriteCount().hashCode());
        result = prime * result + ((getUploadTime() == null) ? 0 : getUploadTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", videoId=").append(videoId);
        sb.append(", userId=").append(userId);
        sb.append(", fileId=").append(fileId);
        sb.append(", videoTitle=").append(videoTitle);
        sb.append(", coverFileId=").append(coverFileId);
        sb.append(", videoDescription=").append(videoDescription);
        sb.append(", duration=").append(duration);
        sb.append(", likeCount=").append(likeCount);
        sb.append(", commentCount=").append(commentCount);
        sb.append(", watchCount=").append(watchCount);
        sb.append(", favoriteCount=").append(favoriteCount);
        sb.append(", uploadTime=").append(uploadTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}