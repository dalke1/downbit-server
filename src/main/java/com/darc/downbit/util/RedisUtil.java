package com.darc.downbit.util;

import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.exception.CommentNotFoundException;
import com.darc.downbit.common.exception.NoSuchVideoException;
import com.darc.downbit.common.po.VideoTimePo;
import com.darc.downbit.dao.entity.Comment;
import com.darc.downbit.dao.mapper.VideoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/20-17:50:49
 * @description
 */
@Component
@Slf4j
public class RedisUtil {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private MongoTemplate mongoTemplate;

    public <T> RBloomFilter<T> getBloomFilter(String name, long expectedInsertions, double falseProbability) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        return bloomFilter;
    }

    public VideoCache getVideoCacheFromRedis(String videoId) {
        RBloomFilter<Object> videoBloomFilter = getBloomFilter("videoBloomFilter", 1000000, 0.03);
        if (!videoBloomFilter.contains(videoId)) {
            throw new NoSuchVideoException("没有该视频,视频id: " + videoId);
        }
        // 从redis中获取视频缓存
        VideoCache videoCache = (VideoCache) redisTemplate.opsForValue().get("videoCache:" + videoId);
        if (videoCache == null) {
            // 如果缓存中没有视频信息，则加锁
            Boolean videoLock = redisTemplate.opsForValue().setIfAbsent("videoCacheLock:" + videoId, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(videoLock)) {
                // 如果取得锁成功，则从mongo中获取视频信息
                videoCache = mongoTemplate.findById(String.valueOf(videoId), VideoCache.class);
                if (videoCache == null) {
                    // 如果mongo中没有视频信息，则从MySql中获取视频信息
                    Integer id = Integer.valueOf(videoId);
                    videoCache = videoMapper.getVideoCacheByVideoId(id);
                    // 如果MySql中也没有视频信息，则抛出异常
                    if (videoCache == null) {
                        throw new NoSuchVideoException("没有该视频,视频id: " + id);
                    }
                    VideoTimePo videoTimePo = videoMapper.selectUploadTimeAndDurationByVideoId(id);
                    videoCache.setUploadTime(videoTimePo.getUploadTime().getTime());
                    videoCache.setDuration(CommonUtil.formatDuration(videoTimePo.getDuration()));
                    videoCache.setTags(videoMapper.getTagsByVideoId(id));
                    videoCache.setCoverFileName(videoMapper.getCoverByVideoId(id));
                    // 将视频信息存入mongoDb
                    mongoTemplate.save(videoCache);
                }
                // 将视频信息存入redis
                redisTemplate.opsForValue().set("videoCache:" + videoId, videoCache, 1, TimeUnit.HOURS);
            } else {
                // 如果取得锁失败，则等待100ms后再次获取视频信息
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.info("{}:{}", e.getClass().getName(), e.getMessage());
                }
                return getVideoCacheFromRedis(videoId);
            }
        } else {
            // 如果缓存中有视频信息，则刷新缓存时间
            redisTemplate.expire("videoCache:" + videoId, 2, TimeUnit.HOURS);
        }
        return videoCache;
    }

    public Comment getCommentFromRedis(String commentId) {
        RBloomFilter<Object> commentBloomFilter = getBloomFilter("commentBloomFilter", 1000000, 0.03);
        // 如果布隆过滤器中没有该评论id，则抛出异常
        if (!commentBloomFilter.contains(commentId)) {
            throw new CommentNotFoundException("没有该评论,评论id: " + commentId);
        }
        // 从redis中获取评论信息
        Comment comment = (Comment) redisTemplate.opsForValue().get("commentCache:" + commentId);
        if (comment == null) {
            // 如果缓存中没有评论信息，则加锁
            Boolean commentLock = redisTemplate.opsForValue().setIfAbsent("commentCacheLock:" + commentId, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(commentLock)) {
                // 如果取得锁成功，则从mongo中获取评论信息
                comment = mongoTemplate.findById(commentId, Comment.class);
                // 将评论信息存入redis
                redisTemplate.opsForValue().set("commentCache:" + commentId, comment, 1, TimeUnit.HOURS);
            } else {
                // 如果取得锁失败，则等待100ms后再次获取评论信息
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.info("{}:{}", e.getClass().getName(), e.getMessage());
                }
                return getCommentFromRedis(commentId);
            }
        } else {
            // 如果缓存中有评论信息，则刷新缓存时间
            redisTemplate.expire("commentCache:" + commentId, 2, TimeUnit.HOURS);
        }
        return comment;
    }

}
