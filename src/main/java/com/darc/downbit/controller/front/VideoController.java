package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.SearchVideoReq;
import com.darc.downbit.common.dto.rep.UpdateVideoDto;
import com.darc.downbit.common.dto.rep.UploadVideoDto;
import com.darc.downbit.common.dto.rep.VideoReqDto;
import com.darc.downbit.common.dto.resp.SearchVideoResp;
import com.darc.downbit.common.dto.resp.VideoPage;
import com.darc.downbit.common.dto.resp.VideoRespDto;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CosUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/interest_tags")
    public Object getInterestTags() {
        return RestResp.ok(videoService.getInterestTags());
    }

    @GetMapping("/get_new_videos")
    public Object getNewVideos(@RequestParam(value = "videoId", required = false) String videoId) {
        List<VideoRespDto> newVideos = videoService.getNewVideos(videoId);
        if (newVideos != null && !newVideos.isEmpty()) {
            return RestResp.ok(newVideos);
        }
        return RestResp.ok();
    }

    @GetMapping("/get_hot_videos")
    public Object getHotVideos(@RequestParam(value = "index", required = false) Integer index, @RequestParam(value = "copyId", required = false) String copyId) {
        VideoPage hotVideos = videoService.getHotVideos(copyId, index);
        if (hotVideos != null) {
            log.info("视频信息:{}", hotVideos);
            return RestResp.ok(hotVideos);
        }
        return RestResp.ok();
    }

    @GetMapping("/get_tag_videos/{tag}")
    public Object getTagVideos(@PathVariable("tag") String tag,
                               @RequestParam(value = "index", required = false) Integer index,
                               @RequestParam(value = "copyId", required = false) String copyId) {
        VideoPage tagHotVideos = videoService.getHotVideosByTag(tag, copyId, index);
        if (tagHotVideos != null) {
            return RestResp.ok(tagHotVideos);
        }
        return RestResp.ok();
    }

    @GetMapping("/get_video_info/{videoId}")
    public Object getVideoInfo(@PathVariable("videoId") String videoId) {
        return RestResp.ok(videoService.getVideoInfo(videoId));
    }

    @GetMapping("/get_related_videos/{videoId}")
    public Object getRelatedVideos(@PathVariable("videoId") String videoId) {
        List<VideoRespDto> relatedVideoList = videoService.getRelatedVideoList(videoId);
        if (relatedVideoList.isEmpty()) {
            return RestResp.ok();
        }
        return RestResp.ok(relatedVideoList);
    }

    @GetMapping("/get_update_Info")
    public Object getUpdateInfo(@RequestParam("videoId") String videoId) {
        return RestResp.ok(videoService.getVideoForUpdate(videoId));
    }

    @PostMapping("/update_video")
    public Object updateVideo(@RequestBody @Validated UpdateVideoDto updateVideoDto) {
        videoService.updateVideo(updateVideoDto);
        return RestResp.ok();
    }

    @DeleteMapping("/delete_video")
    public Object deleteVideo(@RequestParam("videoId") String videoId) {
        videoService.deleteVideo(videoId);
        return RestResp.ok();
    }


    @GetMapping("/search")
    public RestResp<SearchVideoResp> searchVideos(
            @RequestParam("query") String query,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "sortBy", defaultValue = "relevance") String sortBy,
            @RequestParam(value = "size", defaultValue = "12") Integer size
    ) {
        SearchVideoReq dto = new SearchVideoReq();
        dto.setQuery(query);
        dto.setPage(page);
        dto.setSortBy(sortBy);
        dto.setSize(size);
        return RestResp.ok(videoService.searchVideos(dto));
    }
}
