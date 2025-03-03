package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.dto.rep.VideoReqDto;
import com.darc.downbit.common.dto.resp.CoverRespDto;
import com.darc.downbit.common.dto.resp.HistoryVideoRespDto;
import com.darc.downbit.common.dto.resp.LikeVideoRespDto;
import com.darc.downbit.common.dto.resp.VideoRespDto;
import com.darc.downbit.common.exception.NoMoreRecommendException;
import com.darc.downbit.config.auth.AuthConfig;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.entity.Video;
import com.darc.downbit.dao.mapper.FavoriteVideoMapper;
import com.darc.downbit.dao.mapper.VideoMapper;
import com.darc.downbit.service.RecommendService;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CommonUtil;
import com.darc.downbit.util.CosUtil;
import com.darc.downbit.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author darc
 * @description 针对表【video(视频表)】的数据库操作Service实现
 * @createDate 2024-12-19 00:18:38
 */
@Service
@Slf4j
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
        implements VideoService {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private FavoriteVideoMapper favoriteVideoMapper;

    @Resource
    private CosUtil cosUtil;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RecommendService recommendService;

    @Override
    public List<CoverRespDto> geUserWorks() {
        User user = AuthConfig.getAuthUser().getUser();
        String username = user.getUsername();
        Integer userId = user.getUserId();
        return videoMapper.getVideosIdByUserId(userId).stream()
                .map(videoId -> {
                    VideoCache videoCache = redisUtil.getVideoCacheFromRedis(String.valueOf(videoId));
                    return new CoverRespDto(
                            videoCache.getVideoId(),
                            videoCache.getVideoTitle(),
                            cosUtil.getCoverUrl(username, videoCache.getCoverFileName())
                    );
                })
                .toList();
    }

    @Override
    public void addHistory(VideoReqDto videoReqDto) {
        User user = AuthConfig.getAuthUser().getUser();
        long timestamp = System.currentTimeMillis();
        String historyKey = "history:" + user.getUsername();
        redisTemplate.opsForZSet().add(historyKey, videoReqDto.getVideoId(), timestamp);
        redisTemplate.opsForHash().increment("watchCount", videoReqDto.getVideoId(), 1);
    }

    @Override
    public List<HistoryVideoRespDto> getHistory() {
        User user = AuthConfig.getAuthUser().getUser();
        String username = user.getUsername();
        String historyKey = "history:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(historyKey))) {
            return null;
        }
        Set<String> historyVideos = redisTemplate.opsForZSet().reverseRange(historyKey, 0, -1);
        if (historyVideos == null || historyVideos.isEmpty()) {
            return Collections.emptyList();
        }
        return historyVideos
                .stream()
                .map(videoId -> {
                    VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
                    return new HistoryVideoRespDto(
                            videoCache.getVideoId(),
                            videoCache.getVideoTitle(),
                            cosUtil.getVideoUrl(username, videoCache.getFileName()),
                            cosUtil.getCoverUrl(username, videoCache.getCoverFileName()),
                            "video/mp4");
                })
                .toList();
    }

    @Override
    public void likeVideo(VideoReqDto videoReqDto) {
        User user = AuthConfig.getAuthUser().getUser();
        String likeKey = "like:" + user.getUsername();
        String videoId = videoReqDto.getVideoId();
        redisTemplate.opsForSet().add(likeKey, videoId);
        redisTemplate.opsForHash().increment("likeCount", videoId, 1);
        redisTemplate.opsForZSet().add("activeVideos", videoId, System.currentTimeMillis());
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
        changeRecommendModel(videoCache.getTags(), user.getUsername(), 2);
    }

    @Override
    public void dislikeVideo(VideoReqDto videoReqDto) {
        User user = AuthConfig.getAuthUser().getUser();
        String likeKey = "like:" + user.getUsername();
        String videoId = videoReqDto.getVideoId();
        redisTemplate.opsForSet().remove(likeKey, videoId);
        redisTemplate.opsForHash().increment("likeCount", videoId, -1);
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
        changeRecommendModel(videoCache.getTags(), user.getUsername(), -2);
    }

    @Override
    public List<LikeVideoRespDto> getLikes() {
        User user = AuthConfig.getAuthUser().getUser();
        String username = user.getUsername();
        String likeKey = "like:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(likeKey))) {
            return null;
        }
        Set<String> likedVideos = redisTemplate.opsForSet().members(likeKey);
        if (likedVideos == null || likedVideos.isEmpty()) {
            return Collections.emptyList();
        }
        return likedVideos.stream()
                .map(videoId -> {
                    VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
                    return new LikeVideoRespDto(
                            videoCache.getVideoId(),
                            videoCache.getVideoTitle(),
                            cosUtil.getCoverUrl(username, videoCache.getCoverFileName()),
                            true
                    );
                })
                .toList();
    }

    @Override
    public List<VideoRespDto> getVideoList() {
        return List.of();
    }

    @Override
    public VideoRespDto getVideo(String videoTitle) {
        return null;
    }

    @Override
    public VideoRespDto recommend() {
        AuthUser authUser = AuthConfig.getAuthUser();
        User user = authUser.getUser();
        String username = user.getUsername();
        Integer userId = user.getUserId();
        String userModelKey = "userModel:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(userModelKey))) {
            return generateRandomVideo(username, userId, authUser.getIsGuest());
        }
        String recommendKey = "recommend:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(recommendKey))) {
            try {
                recommendService.getRecommendVideos(username, userId);
            } catch (NoMoreRecommendException e) {
                log.info(e.getMessage());
                return generateRandomVideo(username, userId, authUser.getIsGuest());
            }
            return generateRandomVideo(username, userId, authUser.getIsGuest());
        }
        //从zset中获取获取评分最高的,并删除
        Set<String> recommend = redisTemplate.opsForZSet().reverseRange(recommendKey, 0, -1);
        if (recommend == null || recommend.isEmpty()) {
            return generateRandomVideo(username, userId, authUser.getIsGuest());
        }
        String videoId = recommend.stream().findFirst().orElse(null);
        redisTemplate.opsForZSet().remove(recommendKey, videoId);
        if (recommend.size() <= 2) {
            recommendService.getRecommendVideos(username, userId);
        }
        return generateVideo(username, userId, videoId);
    }

    @Override
    public void prepareRecommend() {
        AuthUser authUser = AuthConfig.getAuthUser();
        User user = authUser.getUser();
        String username = user.getUsername();
        Integer userId = user.getUserId();
        try {
            recommendService.getRecommendVideos(username, userId);
        } catch (NoMoreRecommendException e) {
            log.info(e.getMessage());
        }
    }

    @Override
    public void changeRecommendModel(List<String> tags, String username, Integer score) {
        String userModelKey = "userModel:" + username;
        tags.forEach(tag -> redisTemplate.opsForHash().increment(userModelKey, tag, score));
    }


    public VideoRespDto generateRandomVideo(String username, Integer userId, Boolean isGuest) {
        Random random = new Random();
        // 获取1-3的随机数,包括1和3
        int randomNum = random.nextInt(3) + 1;
        return produceVideo(username, userId, String.valueOf(randomNum), isGuest);
    }

    public VideoRespDto generateVideo(String username, Integer userId, String videoId) {
        return produceVideo(username, userId, videoId, false);
    }

    public VideoRespDto produceVideo(String username, Integer userId, String videoId, Boolean isGuest) {
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(String.valueOf(videoId));
        String uploader = videoCache.getUploader();
        String videoUrl = cosUtil.getVideoUrl(uploader, videoCache.getFileName());
        String coverUrl = cosUtil.getCoverUrl(uploader, videoCache.getCoverFileName());
        String videoTitle = videoCache.getVideoTitle();
        boolean isLike = !isGuest && Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("like:" + username, videoId));
        boolean isFavorite = !isGuest && favoriteVideoMapper.isVideoInFavoriteByVideoTitle(userId, videoTitle) > 0;
        Integer likeCount = (Integer) redisTemplate.opsForHash().get("likeCount", videoId);
        if (likeCount == null) {
            likeCount = 0;
        }
        Integer commentCount = (Integer) redisTemplate.opsForHash().get("commentCount", videoId);
        if (commentCount == null) {
            commentCount = 0;
        }
        Integer watchCount = (Integer) redisTemplate.opsForHash().get("watchCount", videoId);
        if (watchCount == null) {
            watchCount = 0;
        }
        Integer favoriteCount = (Integer) redisTemplate.opsForHash().get("favoriteCount", videoId);
        if (favoriteCount == null) {
            favoriteCount = 0;
        }

        String uploadTimeStr = CommonUtil.formatTimeString(videoCache.getUploadTime().getTime());
        return new VideoRespDto(
                videoCache.getVideoId(),
                videoTitle,
                coverUrl,
                videoUrl,
                "video/mp4",
                likeCount,
                commentCount,
                watchCount,
                favoriteCount,
                uploader,
                uploadTimeStr,
                isLike,
                isFavorite,
                videoCache.getTags());
    }

}




