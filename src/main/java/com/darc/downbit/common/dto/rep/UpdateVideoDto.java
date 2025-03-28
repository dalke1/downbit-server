package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/28-02:50:32
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVideoDto {
    @NotNull(message = "视频ID不能为空")
    @NotBlank(message = "视频ID不能为空格")
    private String videoId;
    private String videoTitle;
    private String videoDescription;
    private List<String> tags;
}
