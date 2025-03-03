package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-00:30:55
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryVideoRespDto {
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
}
