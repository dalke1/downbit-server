package com.darc.downbit.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author darc
 * @TableName favorite
 */
@TableName(value = "favorite")
@Data
public class Favorite implements Serializable {
    /**
     * 收藏夹id
     */
    @TableId(type = IdType.AUTO)
    private Integer favoriteId;

    /**
     * 收藏夹名称
     */
    private String favoriteName;

    /**
     * 用户id
     */
    private Integer userId;

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
        Favorite other = (Favorite) that;
        return (this.getFavoriteId() == null ? other.getFavoriteId() == null : this.getFavoriteId().equals(other.getFavoriteId()))
                && (this.getFavoriteName() == null ? other.getFavoriteName() == null : this.getFavoriteName().equals(other.getFavoriteName()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFavoriteId() == null) ? 0 : getFavoriteId().hashCode());
        result = prime * result + ((getFavoriteName() == null) ? 0 : getFavoriteName().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", favoriteId=").append(favoriteId);
        sb.append(", favoriteName=").append(favoriteName);
        sb.append(", userId=").append(userId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}