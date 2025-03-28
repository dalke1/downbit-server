package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/19-21:29:54
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPage {
    private List<VideoRespDto> videos;
    private String copyId;
}
