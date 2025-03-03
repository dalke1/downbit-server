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
    @JsonProperty("videoType")
    private String videoType;
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

    @JsonIgnore
    private String videoKey;
    @JsonIgnore
    private String coverKey;

    // 按照所有json字段的顺序生成构造器
    public VideoRespDto(String videoId, String title, String coverUrl, String videoUrl, String videoType, Integer likeCount, Integer commentCount, Integer watchCount, Integer favoriteCount, String uploader, String uploadTime, Boolean isLike, Boolean isFavorite, List<String> tags) {
        this.videoId = videoId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
        this.videoType = videoType;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.watchCount = watchCount;
        this.favoriteCount = favoriteCount;
        this.uploader = uploader;
        this.uploadTime = uploadTime;
        this.isLike = isLike;
        this.isFavorite = isFavorite;
        this.tags = tags;
    }
}
