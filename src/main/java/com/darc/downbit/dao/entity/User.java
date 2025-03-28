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
import java.util.Objects;

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

    @JsonProperty("intro")
    private String intro;

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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(nickname, user.nickname) && Objects.equals(mail, user.mail) && Objects.equals(avatarId, user.avatarId) && Objects.equals(phone, user.phone) && Objects.equals(ip, user.ip) && Objects.equals(device, user.device) && Objects.equals(intro, user.intro) && Objects.equals(uuid, user.uuid) && Objects.equals(createdTime, user.createdTime) && Objects.equals(updatedTime, user.updatedTime) && Objects.equals(role, user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, password, nickname, mail, avatarId, phone, ip, device, intro, uuid, createdTime, updatedTime, role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", mail='" + mail + '\'' +
                ", avatarId=" + avatarId +
                ", phone='" + phone + '\'' +
                ", ip='" + ip + '\'' +
                ", device='" + device + '\'' +
                ", intro='" + intro + '\'' +
                ", uuid='" + uuid + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", role='" + role + '\'' +
                '}';
    }
}