package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/20-23:55:30
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoRespDto {
    @JsonProperty("videoId")
    private String videoId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("coverUrl")
    private String coverUrl;
    @JsonProperty("videoUrl")
    private String videoUrl;
    @JsonProperty("videoFormat")
    private String videoFormat;
    @JsonProperty("likeCount")
    private Integer likeCount;
    @JsonProperty("commentCount")
    private Integer commentCount;
    @JsonProperty("watchCount")
    private Integer watchCount;
    @JsonProperty("favoriteCount")
    private Integer favoriteCount;
    @JsonProperty("uploader")
    private String uploader;
    @JsonProperty("uploadTime")
    private String uploadTime;
    @JsonProperty("isLike")
    private Boolean isLike;
    @JsonProperty("isFavorite")
    private Boolean isFavorite;
    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("duration")
    private String duration;
    @JsonProperty("uploaderAvatar")
    private String uploaderAvatar;
    @JsonProperty("videoDescription")
    private String videoDescription;

    @JsonIgnore
    private String videoKey;
    @JsonIgnore
    private String coverKey;

    public VideoRespDto(String videoId, String title, String coverUrl, String videoUrl, String videoFormat, Integer likeCount, Integer commentCount, Integer watchCount, Integer favoriteCount, String uploader, String uploadTime, Boolean isLike, Boolean isFavorite, List<String> tags, String duration, String uploaderAvatar, String videoDescription) {
        this.videoId = videoId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
        this.videoFormat = videoFormat;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.watchCount = watchCount;
        this.favoriteCount = favoriteCount;
        this.uploader = uploader;
        this.uploadTime = uploadTime;
        this.isLike = isLike;
        this.isFavorite = isFavorite;
        this.tags = tags;
        this.duration = duration;
        this.uploaderAvatar = uploaderAvatar;
        this.videoDescription = videoDescription;
    }
}
