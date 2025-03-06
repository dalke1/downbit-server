package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.UploadVideoDto;
import com.darc.downbit.common.dto.rep.VideoReqDto;
import com.darc.downbit.common.dto.resp.VideoRespDto;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CosUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/20-06:00:55
 * @description
 */
@RestController
@RequestMapping("/api/video")
@Slf4j
public class VideoController {
    @Resource
    VideoService videoService;

    @Resource
    CosUtil cosUtil;

    @GetMapping("/get_user_works")
    public Object getLoginUserVideos() {
        return RestResp.ok(videoService.geUserWorks());
    }

    @PostMapping("/upload")
    public Object uploadVideo(@RequestBody @Validated UploadVideoDto uploadVideoDto) {
        videoService.uploadVideo(uploadVideoDto);
        return RestResp.ok();
    }

    @GetMapping("/get_upload_url/{type}/{fileName}")
    public Object getUploadUrl(@PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        return RestResp.ok(videoService.getUploadUrl(fileName, type));
    }

    @GetMapping("/tags")
    public Object getTags() {
        return RestResp.ok(videoService.getTags());
    }

    @PostMapping("/add_history")
    public void addHistory(@RequestBody @Validated VideoReqDto videoReqDto) {
        videoService.addHistory(videoReqDto);
    }

    @GetMapping("/get_history")
    public Object getHistory() {
        return RestResp.ok(videoService.getHistory());
    }

    @GetMapping("/get_video/{video_title}")
    public Object getVideo(@PathVariable("video_title") String videoTitle) {
        return RestResp.ok(videoService.getVideo(videoTitle));
    }

    @GetMapping("/recommend")
    public Object recommend() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        VideoRespDto videoRespDto = videoService.recommend();
        stopWatch.stop();
        log.info("总推荐耗时: {}", stopWatch.getTotalTimeMillis());
        return RestResp.ok(videoRespDto);
    }

    @PostMapping("/prepare_recommend")
    public Object prepareRecommend() {
        videoService.prepareRecommend();
        return RestResp.ok();
    }

    @PostMapping("/like")
    public void like(@RequestBody @Validated VideoReqDto videoReqDto) {
        videoService.likeVideo(videoReqDto);
    }

    @PostMapping("/dislike")
    public void dislike(@RequestBody @Validated VideoReqDto videoReqDto) {
        videoService.dislikeVideo(videoReqDto);
    }

    @GetMapping("/get_likes")
    public Object getLikes() {
        return RestResp.ok(videoService.getLikes());
    }

    @GetMapping("/get_videos")
    public Object getVideoList() {
        return RestResp.ok();
    }
}
