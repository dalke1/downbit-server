package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/14-22:03:33
 * @description
 */
@Data
@AllArgsConstructor
public class CommentReqDto {
    @NotNull(message = "视频ID不能为空")
    @NotBlank(message = "视频ID不能为空格")
    private String videoId;
    @NotNull(message = "评论内容不能为空")
    @NotBlank(message = "评论内容不能为空格")
    private String commentText;
}
