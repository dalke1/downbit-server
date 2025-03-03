package com.darc.downbit.common.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-13:59:02
 * @description
 */
@Data
public class RecommendRespDto {
    @JsonProperty("recommendations")
    private List<Recommend> recommendations;
}
