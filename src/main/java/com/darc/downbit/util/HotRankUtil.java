package com.darc.downbit.util;

import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.dao.entity.Comment;
import com.darc.downbit.dao.mapper.VideoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/21-01:57:11
 * @description
 */
@Slf4j
@Component
public class HotRankUtil {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private MongoTemplate mongoTemplate;

    // 半衰期（小时）
    private static final double HALF_LIFE = 24.0;
    // 各项行为的初始权重
    private static final double WATCH_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 2.0;
    private static final double FAVORITE_WEIGHT = 3.0;
    private static final double COMMENT_WEIGHT = 2.0;

    public void calculateHotVideoScore(List<String> videoIdList) {
        if (videoIdList == null || videoIdList.isEmpty()) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String videoId : videoIdList) {
                // 获取视频信息
                VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
                // 获取各类计数
                Integer watchCount = (Integer) redisTemplate.opsForHash().get("watchCount", videoId);
                Integer likeCount = (Integer) redisTemplate.opsForHash().get("likeCount", videoId);
                Integer favoriteCount = (Integer) redisTemplate.opsForHash().get("favoriteCount", videoId);
                Integer commentCount = (Integer) redisTemplate.opsForHash().get("commentCount", videoId);
                Double activeTime = redisTemplate.opsForZSet().score("activeVideos", videoId);
                // 计算时间衰减
                long currentTime = System.currentTimeMillis();
                double hoursPassed;
                if (activeTime == null) {
                    double uploadTime = (double) videoCache.getUploadTime().getTime();
                    hoursPassed = (currentTime - uploadTime) / 1000.0 / 3600.0;
                } else {
                    hoursPassed = (currentTime - activeTime) / 1000.0 / 3600.0;
                }
                double decayFactor = Math.pow(0.5, hoursPassed / HALF_LIFE);

                // 计算热度分数
                double hotScore = ((watchCount == null ? 0 : watchCount) * WATCH_WEIGHT +
                        (likeCount == null ? 0 : likeCount) * LIKE_WEIGHT +
                        (favoriteCount == null ? 0 : favoriteCount) * FAVORITE_WEIGHT +
                        (commentCount == null ? 0 : commentCount) * COMMENT_WEIGHT) * decayFactor;

                // 打印所有的数值
                log.info("videoId: {}, watchCount: {}, likeCount: {}, favoriteCount: {}, commentCount: {}, activeTime: {}, hoursPassed: {}, decayFactor: {}, hotScore: {}",
                        videoId, watchCount, likeCount, favoriteCount, commentCount, activeTime, hoursPassed, decayFactor, hotScore);

                // 批量添加到Redis
                redisTemplate.opsForZSet().add("hotVideos:全站", videoId, hotScore);

                // 按标签添加
                for (String tag : videoCache.getTags()) {
                    redisTemplate.opsForZSet().add("hotVideos:" + tag, videoId, hotScore);
                }
            }
            return null;
        });
    }

    private static final int RECENT_VIDEOS_COUNT = 10;

    public void calculateHotUploaderScore(List<Integer> userIdList) {
        if (userIdList == null || userIdList.isEmpty()) {
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Integer userId : userIdList) {
                // 获取用户最近10个视频
                List<Integer> recentVideosId = videoMapper.getVideosIdByUserIdSortByUploadTime(userId, RECENT_VIDEOS_COUNT);

                if (recentVideosId.isEmpty()) {
                    continue;
                }

                long currentTime = System.currentTimeMillis();

                Double activeTime = redisTemplate.opsForZSet().score("activeVideos", recentVideosId.getFirst().toString());
                // 计算时间衰减
                double hoursPassed;
                if (activeTime == null) {
                    long uploadTime = redisUtil.getVideoCacheFromRedis(String.valueOf(recentVideosId.getFirst())).getUploadTime().getTime();
                    hoursPassed = (currentTime - uploadTime) / 1000.0 / 3600.0;
                } else {
                    hoursPassed = (currentTime - activeTime) / 1000.0 / 3600.0;
                }
                double decayFactor = Math.pow(0.5, hoursPassed / HALF_LIFE);

                // 从redis中获取所有视频对应的热度,如果没有则为0,然后累加
                double totalHotScore = recentVideosId.stream()
                        .mapToDouble(videoId -> {
                            Double hotScore = redisTemplate.opsForZSet().score("hotVideos:全站", videoId);
                            return hotScore == null ? 0 : hotScore;
                        })
                        .sum();
                redisTemplate.opsForZSet().add("hotUploader", String.valueOf(userId), totalHotScore * decayFactor);
            }
            return null;
        });
    }

    public void calculateHotComment(List<String> videoIdList) {
        if (videoIdList == null || videoIdList.isEmpty()) {
            return;
        }
        for (String videoId : videoIdList) {
            Query queryParent = new Query(Criteria
                    .where("parentId").exists(false)
                    .and("replyTo").exists(false)
                    .and("videoId").is(videoId)
            );
            List<Comment> parentCommentList = mongoTemplate.find(queryParent, Comment.class);
            if (parentCommentList.isEmpty()) {
                continue;
            }
            Map<String, Double> hotCommentsMap = parentCommentList.stream()
                    .collect(Collectors.toMap(
                            Comment::getId,
                            comment -> {
                                Query queryReplies = new Query(Criteria.where("parentId").is(comment.getId()));
                                return comment.getLikeCount() * LIKE_WEIGHT + mongoTemplate.count(queryReplies, Comment.class);
                            }
                    ));
            String hotCommentKey = "hotComments:" + videoId;
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                hotCommentsMap.forEach((key, value) -> redisTemplate.opsForZSet().add(hotCommentKey, key, value));
                return null;
            });
        }
    }

    public List<String> getHotVideos(int limit) {
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange("hotVideos:全站", 0, limit - 1);
        if (objects != null && !objects.isEmpty()) {
            return objects.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

    public List<String> getHotVideosByTag(int limit, String tag) {
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange("hotVideos:" + tag, 0, limit - 1);
        if (objects != null && !objects.isEmpty()) {
            return objects.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

    public List<String> getHotUploader(int limit) {
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange("hotUploader", 0, limit - 1);
        if (objects != null && !objects.isEmpty()) {
            return objects.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

    public List<String> getHotCommentsByVideoId(int start, int end, String videoId) {
        String hotCommentKey = "hotComments:" + videoId;
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange(hotCommentKey, start, end);
        if (objects != null && !objects.isEmpty()) {
            return objects.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }
}
