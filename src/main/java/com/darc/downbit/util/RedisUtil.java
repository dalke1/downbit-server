package com.darc.downbit.util;

import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.exception.CommentNotFoundException;
import com.darc.downbit.common.exception.NoSuchVideoException;
import com.darc.downbit.dao.entity.Comment;
import com.darc.downbit.dao.mapper.VideoMapper;
import jakarta.annotation.Resource;
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
        VideoCache videoCache = (VideoCache) redisTemplate.opsForValue().get("videoCache:" + videoId);
        if (videoCache == null) {
            videoCache = mongoTemplate.findById(String.valueOf(videoId), VideoCache.class);
            if (videoCache == null) {
                Integer id = Integer.valueOf(videoId);
                videoCache = videoMapper.getVideoCacheByVideoId(id);
                if (videoCache == null) {
                    throw new NoSuchVideoException("没有该视频,视频id: " + id);
                }
                videoCache.setTags(videoMapper.getTagsByVideoId(id));
                videoCache.setCoverFileName(videoMapper.getCoverByVideoId(id));
                mongoTemplate.save(videoCache);
            }
            redisTemplate.opsForValue().set("videoCache:" + videoId, videoCache, 1, TimeUnit.HOURS);
        } else {
            redisTemplate.expire("videoCache:" + videoId, 2, TimeUnit.HOURS);
        }
        return videoCache;
    }

    public Comment getCommentFromRedis(String commentId) {
        Comment comment = (Comment) redisTemplate.opsForValue().get("commentCache:" + commentId);
        if (comment == null) {
            comment = mongoTemplate.findById(commentId, Comment.class);
            if (comment == null) {
                throw new CommentNotFoundException("没有该评论,评论id: " + commentId);
            }
            redisTemplate.opsForValue().set("commentCache:" + commentId, comment, 1, TimeUnit.HOURS);
        } else {
            redisTemplate.expire("commentCache:" + commentId, 2, TimeUnit.HOURS);
        }
        return comment;
    }
}
