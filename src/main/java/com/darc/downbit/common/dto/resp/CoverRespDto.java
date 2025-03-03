package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-00:30:32
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoverRespDto {
    @JsonProperty("videoId")
    private String videoId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("coverUrl")
    private String coverUrl;
}
