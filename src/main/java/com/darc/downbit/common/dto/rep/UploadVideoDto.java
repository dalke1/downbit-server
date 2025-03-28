package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/5-18:43:11
 * @description
 */
@Data
public class UploadVideoDto {
    @NotNull(message = "视频标题不能为空")
    @NotBlank(message = "视频标题不能为空格")
    private String videoTitle;
    @NotNull(message = "视频描述不能为空")
    @NotBlank(message = "视频描述不能为空格")
    private String videoDescription;
    @NotNull(message = "视频文件名不能为空")
    @NotBlank(message = "视频文件名不能为空格")
    private String videoFileName;
    @NotNull(message = "封面文件名不能为空")
    @NotBlank(message = "封面文件名不能为空格")
    private String coverFileName;
    @NotEmpty(message = "视频标签不能为空")
    private List<String> tags;
    @NotNull(message = "视频格式不能为空")
    @NotBlank(message = "视频格式不能为空格")
    private String format;
    @Min(value = 0, message = "视频时长不能小于0")
    private Integer duration;
}
