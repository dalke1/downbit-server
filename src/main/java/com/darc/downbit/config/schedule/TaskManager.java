package com.darc.downbit.config.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darc.downbit.dao.mapper.VideoMapper;
import com.darc.downbit.util.HotRankUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/23-21:37:38
 * @description
 */
@Component
@Slf4j
public class TaskManager {

    @Resource
    private HotRankUtil hotRankUtil;

    @Resource
    private VideoMapper videoMapper;

    @Scheduled(cron = "0 30 */2 * * ?")
    @PostConstruct
    public void calculateHotVideos() {
        log.info("重新计算热门视频");
        List<String> videoIdList = videoMapper.selectList(new QueryWrapper<>()).stream()
                .map(video -> String.valueOf(video.getVideoId()))
                .toList();
        hotRankUtil.calculateHotVideoScore(videoIdList);
    }

    @Scheduled(cron = "0 30 */1 * * ?")
    @PostConstruct
    public void calculateHotComments() {
        log.info("重新计算热门评论");
        List<String> videoIdlist = videoMapper.selectList(new QueryWrapper<>()).stream()
                .map(video -> String.valueOf(video.getVideoId()))
                .toList();
        hotRankUtil.calculateHotCommentScore(videoIdlist);
    }
}
