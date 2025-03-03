package com.darc.downbit.service;

import com.darc.downbit.common.dto.rep.CommentReqDto;
import com.darc.downbit.common.dto.rep.ReplyReqDto;
import com.darc.downbit.common.dto.resp.CommentRespDto;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/14-22:01:08
 * @description
 */
public interface CommentService {
    List<CommentRespDto> getHotComments(String videoTitle, Integer startIndex);

    List<CommentRespDto> getNewComments(String videoTitle, Integer startIndex);

    List<CommentRespDto> getReplies(String commentId);

    Integer getCommentCount(String videoTitle);

    void addComment(CommentReqDto commentReqDto);

    void addReply(ReplyReqDto replyReqDto);

    void likeComment(String commentId, Boolean isParent);

    void dislikeComment(String commentId, Boolean isParent);
}
