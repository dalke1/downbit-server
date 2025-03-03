package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-14:00:43
 * @description
 */
@Data
public class Recommend {
    @JsonProperty("video_title")
    private String videoTitle;
    @JsonProperty("score")
    private Float score;
}
