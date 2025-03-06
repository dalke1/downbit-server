package com.darc.downbit.service.impl;

import com.darc.downbit.common.dto.rep.RecommendReqDto;
import com.darc.downbit.common.dto.resp.RecommendRespDto;
import com.darc.downbit.common.exception.JsonException;
import com.darc.downbit.common.exception.NoMoreRecommendException;
import com.darc.downbit.config.htttp.RecommendApi;
import com.darc.downbit.dao.mapper.FavoriteVideoMapper;
import com.darc.downbit.dao.mapper.VideoMapper;
import com.darc.downbit.service.RecommendService;
import com.darc.downbit.util.HotRankUtil;
import com.darc.downbit.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/23-22:03:21
 * @description
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private HotRankUtil hotRankUtil;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private FavoriteVideoMapper favoriteVideoMapper;
    @Resource
    private RecommendApi recommendApi;

    @Async("asyncTaskExecutor")
    @Override
    public void getRecommendVideos(String username, Integer userId) throws NoMoreRecommendException {
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        String recommendKey = "recommend:" + username;

        // 获取用户的标签模型
        Map<Object, Object> userModel = redisTemplate.opsForHash().entries("userModel:" + username);
        // 按分数排序获取前3个标签
        List<String> topTags = userModel.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(
                        Double.parseDouble(String.valueOf(e2.getValue())),
                        Double.parseDouble(String.valueOf(e1.getValue()))
                ))
                .limit(3)
                .map(entry -> String.valueOf(entry.getKey()))
                .toList();

        // 获取前3个标签的热门视频,并取并集
        Set<String> videoIdSet = new HashSet<>();
        for (String tag : topTags) {
            List<String> hotTagVideos = hotRankUtil.getHotVideosByTag(-1, tag);
            videoIdSet.addAll(hotTagVideos);
        }

        // 获取布隆过滤器
        RBloomFilter<Object> bloomFilter = redisUtil.getBloomFilter("bloomFilter:" + username, 10000, 0.01);
        // 获取用户的七天内历史观看记录
        Set<String> historyVideos = redisTemplate.opsForZSet().reverseRangeByScore("history:" + username,
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7), System.currentTimeMillis());

        if (redisTemplate.hasKey(recommendKey)) {
            Long size = redisTemplate.opsForZSet().size(recommendKey);
            if (size != null && size <= 1) {
                Set<String> storedRecommend = redisTemplate.opsForZSet().reverseRangeByScore(recommendKey, 0, -1);
                if (storedRecommend != null) {
                    storedRecommend.forEach(bloomFilter::add);
                    videoIdSet = videoIdSet.stream().filter(videoId -> !bloomFilter.contains(videoId)).collect(Collectors.toSet());
                }
            }
        }

        // 过滤掉用户已经观看过的视频,并取前20个
        if (historyVideos != null && !historyVideos.isEmpty()) {
            historyVideos.forEach(bloomFilter::add);
            videoIdSet = videoIdSet.stream()
                    .filter(videoId -> !bloomFilter.contains(videoId))
                    .limit(20)
                    .collect(Collectors.toSet());
        } else {
            videoIdSet = videoIdSet.stream().limit(20).collect(Collectors.toSet());
        }

        // 如果不足20个,则从全站热门视频补充至20个
        if (videoIdSet.size() < 20) {
            List<String> globalHotVideos = hotRankUtil.getHotVideos(-1);

            videoIdSet.addAll(globalHotVideos.stream()
                    .filter(videoId -> !bloomFilter.contains(videoId))
                    .limit(20 - videoIdSet.size())
                    .collect(Collectors.toSet()));
        }

        // 如果还是不足10个,则从最新上传的视频中补充至10个
        if (videoIdSet.size() < 10) {
            Set<String> latestVideosSet = videoMapper.getVideosIdInTagsSortedByUploadTime(topTags, 100)
                    .stream()
                    .map(String::valueOf)
                    .filter(videoId -> !bloomFilter.contains(videoId))
                    .limit(20 - videoIdSet.size())
                    .collect(Collectors.toSet());
            videoIdSet.addAll(latestVideosSet);
        }
        bloomFilter.delete();
        // 如果最终不足2个,则抛出异常
        if (videoIdSet.size() <= 1) {
            throw new NoMoreRecommendException("没有更多推荐了");
        }

        // 组装视频标题和标签
        Map<String, List<String>> videos = videoIdSet.stream()
                .collect(Collectors.toMap(
                        videoId -> redisUtil.getVideoCacheFromRedis(videoId).getVideoTitle(),
                        videoId -> redisUtil.getVideoCacheFromRedis(videoId).getTags()
                ));
        // TODO 添加关键词列表
        List<String> keyWords = List.of();
        RecommendReqDto recommendReqDto = new RecommendReqDto();
        //设置推荐数量

        recommendReqDto.setVideos(videos);
        recommendReqDto.setRecommendCount(5);
        recommendReqDto.setKeyWords(keyWords);
        // 测试keyWords模式
        if (!keyWords.isEmpty()) {
            recommendReqDto.setUserModel(Map.of());
            recommendReqDto.setLikeVideos(List.of());
            recommendReqDto.setFavoriteVideos(List.of());
        } else {
            recommendReqDto.setUserModel(userModel);
            Set<String> likeVideosSet = redisTemplate.opsForSet().members("like:" + username);
            if (likeVideosSet == null || likeVideosSet.isEmpty()) {
                likeVideosSet = Set.of();
            }
            List<String> favoriteVideos = favoriteVideoMapper.getAllFavoriteVideoTitlesByUserId(userId);
            if (favoriteVideos == null || favoriteVideos.isEmpty()) {
                favoriteVideos = List.of();
            }
            // 喜欢和收藏可以合并
            recommendReqDto.setLikeVideos(likeVideosSet.stream().toList());
            recommendReqDto.setFavoriteVideos(favoriteVideos);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        RecommendRespDto recommendRespDto;
        stopWatch1.stop();
        log.info("处理视频时间:{}ms", stopWatch1.getTotalTimeMillis());
        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        try {
            String json = objectMapper.writeValueAsString(recommendReqDto);
            String recommendResultJson = recommendApi.getRecommend(json).block();
            recommendRespDto = objectMapper.readValue(recommendResultJson, RecommendRespDto.class);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        } catch (WebClientRequestException e) {
            log.error("{}:{}", e.getClass(), e.getMessage());
            throw new NoMoreRecommendException("推荐服务器异常");
        }
        stopWatch2.stop();
        log.info("推荐视频耗时:{}ms", stopWatch2.getTotalTimeMillis());

        recommendRespDto.getRecommendations().forEach(recommendVideo -> {
                    Integer videoId = videoMapper.getVideoIdByVideoTitle(recommendVideo.getVideoTitle());
                    redisTemplate.opsForZSet()
                            .add(recommendKey, String.valueOf(videoId), recommendVideo.getScore());
                }
        );
        redisTemplate.expire(recommendKey, 2, TimeUnit.HOURS);
    }
}
