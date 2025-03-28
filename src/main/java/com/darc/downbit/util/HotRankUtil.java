package com.darc.downbit.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.dao.entity.Comment;
import com.darc.downbit.dao.entity.Tag;
import com.darc.downbit.dao.mapper.TagMapper;
import com.darc.downbit.dao.mapper.VideoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private TagMapper tagMapper;
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

        String currentGlobalCopyId = UUID.randomUUID().toString();
        String globalHotKey = "hotVideos:全站:" + currentGlobalCopyId;


        List<Tag> tags = tagMapper.selectList(new QueryWrapper<>());
        Map<String, String> copyIdMap = new HashMap<>();
        Map<String, String> tagHotKeyMap = new HashMap<>();
        tags.forEach(tag -> {
            String tagCopyId = UUID.randomUUID().toString();
            String tagName = tag.getTagName();
            copyIdMap.put(tagName, tagCopyId);
            tagHotKeyMap.put(tagName, "hotVideos:标签:" + tagName + ":" + tagCopyId);
        });


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
                    long uploadTime = videoMapper.selectUploadTimeAndDurationByVideoId(Integer.parseInt(videoId)).getUploadTime().getTime();
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

                BigDecimal hotScoreScaled = new BigDecimal(hotScore).setScale(2, RoundingMode.HALF_UP);

                // 打印所有的数值
                log.info("videoId: {}, watchCount: {}, likeCount: {}, favoriteCount: {}, commentCount: {}, activeTime: {}, hoursPassed: {}, decayFactor: {}, hotScore: {}",
                        videoId, watchCount, likeCount, favoriteCount, commentCount, activeTime, hoursPassed, decayFactor, hotScore);

                // 批量添加到Redis
                redisTemplate.opsForZSet().add(globalHotKey, videoId, hotScoreScaled.doubleValue());

                // 按标签添加
                for (String tag : videoCache.getTags()) {
                    redisTemplate.opsForZSet().add(tagHotKeyMap.get(tag), videoId, hotScoreScaled.doubleValue());
                }
            }

            redisTemplate.opsForHash().put("currentCopyIdMap", "全站", currentGlobalCopyId);
            redisTemplate.expire(globalHotKey, 3, TimeUnit.HOURS);
            tagHotKeyMap.forEach((key, value) -> redisTemplate.expire(value, 3, TimeUnit.HOURS));
            copyIdMap.forEach((key, value) -> redisTemplate.opsForHash().put("currentCopyIdMap", key, value));

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
                    long uploadTime = videoMapper.selectUploadTimeAndDurationByVideoId(recentVideosId.getFirst()).getUploadTime().getTime();
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

    public void calculateHotCommentScore(List<String> videoIdList) {
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
            String commentCopyId = UUID.randomUUID().toString();
            if (parentCommentList.isEmpty()) {
                redisTemplate.opsForHash().put("currentCommentCopyIdMap", videoId, commentCopyId);
                continue;
            }
            String hotCommentKey = "hotComments:" + videoId + ":copyId:" + commentCopyId;
            Map<String, Double> hotCommentsScoreMap = parentCommentList.stream()
                    .collect(Collectors.toMap(
                            Comment::getId,
                            comment -> {
                                Query queryReplies = new Query(Criteria.where("parentId").is(comment.getId()));
                                return comment.getLikeCount() * LIKE_WEIGHT + mongoTemplate.count(queryReplies, Comment.class);
                            }
                    ));
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                hotCommentsScoreMap.forEach((key, value) -> redisTemplate.opsForZSet().add(hotCommentKey, key, value));
                redisTemplate.opsForHash().put("currentCommentCopyIdMap", videoId, commentCopyId);
                return null;
            });
            redisTemplate.expire(hotCommentKey, 2, TimeUnit.HOURS);
        }


    }

    public List<String> getHotVideos(int start, int end, String hotVideoKey) {
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange(hotVideoKey, start, end);
        return processResult(objects);
    }

    public List<String> getHotUploader(int limit) {
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange("hotUploader", 0, limit - 1);
        return processResult(objects);
    }

    public List<String> getHotComments(int start, int end, String hotCommentKey) {
        Set<Object> objects = redisTemplate.opsForZSet().reverseRange(hotCommentKey, start, end);
        return processResult(objects);
    }

    public List<String> processResult(Set<Object> objects) {
        if (objects != null && !objects.isEmpty()) {
            return objects.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

}
