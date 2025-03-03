package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/26-04:54:49
 * @description
 */
@Data
public class VideoReqDto {
    @NotNull(message = "视频ID不能为空")
    @NotBlank(message = "视频ID不能为空格")
    private String videoId;
}
