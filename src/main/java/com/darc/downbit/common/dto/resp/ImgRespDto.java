package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/21-00:50:40
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImgRespDto {
    @JsonProperty("imgName")
    private String imgName;
    @JsonProperty("imgUrl")
    private String imgUrl;

    public ImgRespDto(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
