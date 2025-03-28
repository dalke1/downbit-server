package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/28-07:22:34
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInfo {
    private String videoId;
    private String title;
    private String videoDescription;
    private List<TagRespDto> tags;
}
