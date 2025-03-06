package com.darc.downbit.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author darc
 */

@TableName(value = "user")
@Data
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("mail")
    private String mail;

    @JsonProperty("avatar_id")
    private Integer avatarId;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("device")
    private String device;

    @JsonProperty("uuid")
    @TableField(exist = false)
    private String uuid;

    @JsonIgnore
    private Date createdTime;

    @JsonIgnore
    private Date updatedTime;

    @TableField(exist = false)
    @JsonProperty("role")
    private String role;

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
        User other = (User) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
                && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
                && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
                && (this.getMail() == null ? other.getMail() == null : this.getMail().equals(other.getMail()))
                && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
                && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
                && (this.getDevice() == null ? other.getDevice() == null : this.getDevice().equals(other.getDevice()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getMail() == null) ? 0 : getMail().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getDevice() == null) ? 0 : getDevice().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", username=").append(username);
        sb.append(", password=").append(password);
        sb.append(", nickname=").append(nickname);
        sb.append(", mail=").append(mail);
        sb.append(", phone=").append(phone);
        sb.append(", ip=").append(ip);
        sb.append(", device=").append(device);
        sb.append(",role=").append(role);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}