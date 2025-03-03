package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/14-22:42:25
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRespDto {
    private String id;
    private String videoId;
    private String avatar;
    private String nickname;
    private String commentText;
    private Integer likeCount;
    private Boolean isLike;
    private String commentTime;
    private String replyTo;
    private Integer replyCount;
}
