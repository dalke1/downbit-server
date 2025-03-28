package com.darc.downbit.common.dto.rep;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/26-01:15:50
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatedReqDto {
    @JsonProperty("videoTitle")
    private String videoTitle;
    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("videos")
    private Map<String, List<String>> videos;
}
