package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.CommentReqDto;
import com.darc.downbit.common.dto.rep.ReplyReqDto;
import com.darc.downbit.common.dto.resp.CommentRespDto;
import com.darc.downbit.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/15-02:17:06
 * @description
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping("/hot/{video_id}")
    public Object getHotComments(@PathVariable("video_id") String videoId, @RequestParam(defaultValue = "0") Integer startIndex) {
        List<CommentRespDto> hotComments = commentService.getHotComments(videoId, startIndex);
        if (hotComments != null) {
            return RestResp.ok(hotComments);
        }
        return RestResp.ok();
    }

    @GetMapping("/new/{video_id}")
    public Object getNewComments(@PathVariable("video_id") String videoId, @RequestParam(defaultValue = "0") Integer startIndex) {
        List<CommentRespDto> newComments = commentService.getNewComments(videoId, startIndex);
        if (newComments != null) {
            return RestResp.ok(newComments);
        }
        return RestResp.ok();
    }

    @GetMapping("/replies/{parent_id}/{start_index}")
    public Object getReplies(@PathVariable("parent_id") String parentId, @PathVariable("start_index") String startIndex) {
        List<CommentRespDto> replies = commentService.getReplies(parentId, Integer.parseInt(startIndex));
        if (replies != null) {
            return RestResp.ok(replies);
        }
        return RestResp.ok();
    }

    @PostMapping("/add_comment")
    public Object addComment(@RequestBody @Validated CommentReqDto commentReqDto) {
        commentService.addComment(commentReqDto);
        return RestResp.ok();
    }

    @PostMapping("/like_comment/{comment_id}")
    public Object likeComment(@PathVariable("comment_id") String commentId, @RequestParam(defaultValue = "true") Boolean isParent) {
        commentService.likeComment(commentId, isParent);
        return RestResp.ok();
    }

    @PostMapping("/dislike_comment/{comment_id}")
    public Object dislikeComment(@PathVariable("comment_id") String commentId, @RequestParam(defaultValue = "true") Boolean isParent) {
        commentService.dislikeComment(commentId, isParent);
        return RestResp.ok();
    }

    @PostMapping("/reply")
    public Object addReply(@RequestBody @Validated ReplyReqDto replyReqDto) {
        commentService.addReply(replyReqDto);
        return RestResp.ok();
    }
}