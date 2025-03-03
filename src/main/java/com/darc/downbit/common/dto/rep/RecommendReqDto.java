package com.darc.downbit.common.dto.rep;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-11:38:46
 * @description
 */
@Data
public class RecommendReqDto {
    @JsonProperty("userModel")
    private Map<Object, Object> userModel;
    @JsonProperty("videos")
    private Map<String, List<String>> videos;
    @JsonProperty("likeVideos")
    private List<String> likeVideos;
    @JsonProperty("favoriteVideos")
    private List<String> favoriteVideos;
    @JsonProperty("recommend_count")
    private int recommendCount;
    @JsonProperty("keyWords")
    private List<String> keyWords;
}
