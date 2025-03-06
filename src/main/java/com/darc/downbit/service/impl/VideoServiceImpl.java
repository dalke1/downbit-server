package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.dto.rep.UploadVideoDto;
import com.darc.downbit.common.dto.rep.VideoReqDto;
import com.darc.downbit.common.dto.resp.*;
import com.darc.downbit.common.exception.BadRequestException;
import com.darc.downbit.common.exception.NoMoreRecommendException;
import com.darc.downbit.config.auth.AuthConfig;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.*;
import com.darc.downbit.dao.mapper.*;
import com.darc.downbit.service.RecommendService;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CommonUtil;
import com.darc.downbit.util.CosUtil;
import com.darc.downbit.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Resource
    private FileMapper fileMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private VideoTagMapper videoTagMapper;

    @Resource
    private ImgMapper imgMapper;

    @Override
    public List<CoverRespDto> geUserWorks() {
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();
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
    @Transactional
    public void uploadVideo(UploadVideoDto uploadVideoDto) {
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();

        // 视频文件信息
        File videoFile = new File();
        videoFile.setFileName(uploadVideoDto.getVideoFileName());
        videoFile.setFileType("video");
        Date date = new Date();
        videoFile.setCreatedTime(date);
        videoFile.setUpdatedTime(date);

        // 封面文件信息
        File coverFile = new File();
        coverFile.setFileName(uploadVideoDto.getCoverFileName());
        coverFile.setFileType("cover");
        coverFile.setCreatedTime(date);
        coverFile.setUpdatedTime(date);

        // 插入视频和封面文件信息
        fileMapper.insert(videoFile);
        fileMapper.insert(coverFile);

        // 视频信息
        Video video = new Video();
        video.setVideoTitle(uploadVideoDto.getVideoTitle());
        video.setUserId(user.getUserId());
        video.setFileId(videoFile.getFileId());
        video.setCoverFileId(coverFile.getFileId());
        video.setVideoDescription(uploadVideoDto.getVideoDescription());
        video.setUploadTime(date);
        video.setDuration(0);
        video.setLikeCount(0);
        video.setWatchCount(0);
        video.setFavoriteCount(0);
        video.setCommentCount(0);
        // 插入视频信息
        videoMapper.insert(video);


        // 图片信息
        Img img = new Img();
        img.setUserId(user.getUserId());
        img.setFileId(coverFile.getFileId());
        // 插入图片信息
        imgMapper.insert(img);

        // 标签Id列表
        List<Integer> tagIdList = tagMapper.getTagIdsByTagNames(uploadVideoDto.getTags());
        // 插入视频标签关系
        tagIdList.forEach(tagId -> {
            VideoTag videoTag = new VideoTag();
            videoTag.setVideoId(video.getVideoId());
            videoTag.setTagId(tagId);
            videoTagMapper.insert(videoTag);
        });
        redisUtil.getBloomFilter("videoBloomFilter", 1000000, 0.03).add(video.getVideoId().toString());
        redisTemplate.opsForZSet().add("hotVideos:全站", String.valueOf(video.getVideoId()), 0);

        // 按标签添加
        for (String tag : uploadVideoDto.getTags()) {
            redisTemplate.opsForZSet().add("hotVideos:" + tag, String.valueOf(video.getVideoId()), 0);
        }
    }

    @Override
    public String getUploadUrl(String fileName, String type) {
        String username = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser().getUsername();
        if (type == null || fileName == null) {
            throw new BadRequestException("参数错误");
        }
        log.info(type);
        log.info(fileName);
        return switch (type) {
            case "avatar" -> cosUtil.getUploadAvatarUrl(username, fileName);
            case "video" -> cosUtil.getUploadVideoUrl(username, fileName);
            case "cover" -> cosUtil.getUploadCoverUrl(username, fileName);
            default -> throw new BadRequestException("参数错误");
        };
    }

    @Override
    public List<TagRespDto> getTags() {
        return tagMapper.selectList(new QueryWrapper<>()).stream()
                .map(tag -> new TagRespDto(tag.getTagName(), String.valueOf(tag.getTagId())))
                .toList();
    }

    @Override
    public void addHistory(VideoReqDto videoReqDto) {
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();
        long timestamp = System.currentTimeMillis();
        String historyKey = "history:" + user.getUsername();
        redisTemplate.opsForZSet().add(historyKey, videoReqDto.getVideoId(), timestamp);
        redisTemplate.opsForHash().increment("watchCount", videoReqDto.getVideoId(), 1);
    }

    @Override
    public List<HistoryVideoRespDto> getHistory() {
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();
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
                            cosUtil.getCoverUrl(username, videoCache.getCoverFileName()),
                            cosUtil.getVideoUrl(username, videoCache.getFileName()),
                            "video/mp4");
                })
                .toList();
    }

    @Override
    public void likeVideo(VideoReqDto videoReqDto) {
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();
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
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();
        String likeKey = "like:" + user.getUsername();
        String videoId = videoReqDto.getVideoId();
        redisTemplate.opsForSet().remove(likeKey, videoId);
        redisTemplate.opsForHash().increment("likeCount", videoId, -1);
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
        changeRecommendModel(videoCache.getTags(), user.getUsername(), -2);
    }

    @Override
    public List<LikeVideoRespDto> getLikes() {
        User user = Objects.requireNonNull(AuthConfig.getAuthUser()).getUser();
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
        if (authUser == null) {
            return generateRandomVideo(null, null, true);
        }
        User user = authUser.getUser();
        String username = user.getUsername();
        Integer userId = user.getUserId();
        String userModelKey = "userModel:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(userModelKey))) {
            return generateRandomVideo(username, userId, false);
        }
        String recommendKey = "recommend:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(recommendKey))) {
            try {
                recommendService.getRecommendVideos(username, userId);
            } catch (NoMoreRecommendException e) {
                log.info(e.getMessage());
                return generateRandomVideo(username, userId, false);
            }
            return generateRandomVideo(username, userId, false);
        }
        //从zset中获取获取评分最高的,并删除
        Set<String> recommend = redisTemplate.opsForZSet().reverseRange(recommendKey, 0, 1);
        if (recommend == null || recommend.isEmpty()) {
            return generateRandomVideo(username, userId, false);
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
        if (authUser == null) {
            return;
        }
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
        List<Integer> videoIdList = videoMapper.getAllVideoId();
        Random random = new Random();
        // 使用count生成随机数
        int randomNum = random.nextInt(videoIdList.size());
        return produceVideo(username, userId, String.valueOf(videoIdList.get(randomNum)), isGuest);
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
        boolean isLike = !isGuest && username != null && Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("like:" + username, videoId));
        boolean isFavorite = !isGuest && userId != null && favoriteVideoMapper.isVideoInFavoriteByVideoTitle(userId, videoTitle) > 0;
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




