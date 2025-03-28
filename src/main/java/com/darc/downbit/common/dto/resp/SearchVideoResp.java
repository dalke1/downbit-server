package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/29-01:46:08
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchVideoResp {
    private List<VideoRespDto> videos;
    private Long total;
}
