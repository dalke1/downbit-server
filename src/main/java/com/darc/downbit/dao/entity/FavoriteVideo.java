package com.darc.downbit.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收藏夹视频关系表
 *
 * @author darc
 * @TableName favorite_video
 */
@TableName(value = "favorite_video")
@Data
public class FavoriteVideo implements Serializable {
    /**
     * 收藏夹视频关系表id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 收藏夹id
     */
    private Integer favoriteId;

    /**
     * 视频id
     */
    private Integer videoId;

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
        FavoriteVideo other = (FavoriteVideo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getFavoriteId() == null ? other.getFavoriteId() == null : this.getFavoriteId().equals(other.getFavoriteId()))
                && (this.getVideoId() == null ? other.getVideoId() == null : this.getVideoId().equals(other.getVideoId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFavoriteId() == null) ? 0 : getFavoriteId().hashCode());
        result = prime * result + ((getVideoId() == null) ? 0 : getVideoId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", favoriteId=").append(favoriteId);
        sb.append(", videoId=").append(videoId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}