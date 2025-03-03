package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/23-18:03:43
 * @description
 */
@Data
@AllArgsConstructor
public class ReplyReqDto {
    @NotNull(message = "视频ID不能为空")
    @NotBlank(message = "视频ID不能为空格")
    private String videoId;
    @NotNull(message = "评论内容不能为空")
    @NotBlank(message = "评论内容不能为空格")
    private String commentText;
    @NotNull(message = "回复对象不能为空")
    @NotBlank(message = "回复对象不能为空格")
    private String replyTo;
    @NotNull(message = "父评论id不能为空")
    @NotBlank(message = "父评论id不能为空格")
    private String parentId;
}
