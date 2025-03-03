package com.darc.downbit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.darc.downbit.common.dto.rep.VideoReqDto;
import com.darc.downbit.common.dto.resp.CoverRespDto;
import com.darc.downbit.common.dto.resp.HistoryVideoRespDto;
import com.darc.downbit.common.dto.resp.LikeVideoRespDto;
import com.darc.downbit.common.dto.resp.VideoRespDto;
import com.darc.downbit.dao.entity.Video;

import java.util.List;

/**
 * @author darc
 * @description 针对表【video(视频表)】的数据库操作Service
 * @createDate 2024-12-19 00:18:38
 */
public interface VideoService extends IService<Video> {
    List<CoverRespDto> geUserWorks();

    void addHistory(VideoReqDto videoReqDto);

    List<HistoryVideoRespDto> getHistory();

    void likeVideo(VideoReqDto videoReqDto);

    void dislikeVideo(VideoReqDto videoReqDto);

    List<LikeVideoRespDto> getLikes();

    List<VideoRespDto> getVideoList();

    VideoRespDto getVideo(String videoTitle);

    VideoRespDto recommend();

    void prepareRecommend();

    void changeRecommendModel(List<String> tags, String username, Integer score);
}
