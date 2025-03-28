package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.dto.rep.CommentReqDto;
import com.darc.downbit.common.dto.rep.ReplyReqDto;
import com.darc.downbit.common.dto.resp.CommentPage;
import com.darc.downbit.common.dto.resp.CommentRespDto;
import com.darc.downbit.common.exception.BadRequestException;
import com.darc.downbit.common.exception.RefreshPage;
import com.darc.downbit.config.auth.AuthConfig;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.Comment;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.mapper.UserMapper;
import com.darc.downbit.service.CommentService;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CommonUtil;
import com.darc.downbit.util.CosUtil;
import com.darc.downbit.util.HotRankUtil;
import com.darc.downbit.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/14-22:10:12
 * @description
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    public static final int COMMENT_SIZE = 10;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private UserMapper userMapper;

    @Resource
    private VideoService videoService;

    @Resource
    private CosUtil cosUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private HotRankUtil hotRankUtil;


    @Override
    public CommentPage getHotComments(String videoId, Integer startIndex, String commentCopyId) {
        log.info(String.valueOf(startIndex));
        String currentCommentCopyId = (String) redisTemplate.opsForHash().get("currentCommentCopyIdMap", videoId);
        String currentHotCommentKey = "hotComments:" + videoId + ":copyId:" + currentCommentCopyId;
        String previousHotCommentKey = "hotComments:" + videoId + ":copyId:" + commentCopyId;
        List<String> hotCommentIdList;
        boolean isFirstPage = true;
        if (commentCopyId == null) {
            hotCommentIdList = hotRankUtil.getHotComments(0, COMMENT_SIZE - 1, currentHotCommentKey);
            if (hotCommentIdList.isEmpty()) {
                return null;
            }
        } else if (startIndex == null) {
            throw new BadRequestException("分页获取热门评论的索引为空");
        } else {
            if (!redisTemplate.hasKey(previousHotCommentKey)) {
                throw new RefreshPage("用户传递的评论copyId已经过期,请刷新页面");
            }
            hotCommentIdList = hotRankUtil.getHotComments(startIndex, startIndex + COMMENT_SIZE - 1, previousHotCommentKey);
            if (hotCommentIdList.isEmpty()) {
                return null;
            }
            isFirstPage = false;
        }
        List<Comment> commentList = hotCommentIdList.stream()
                .map(commentId -> redisUtil.getCommentFromRedis(commentId))
                .toList();
        return isFirstPage ? new CommentPage(generateCommentsResp(commentList, true), currentCommentCopyId)
                : new CommentPage(generateCommentsResp(commentList, true), commentCopyId);
    }

    @Override
    public List<CommentRespDto> getNewComments(String videoId, String commentId) {
        // 只查询父评论
        Query query = new Query(Criteria.where("videoId").is(videoId)
                .and("parentId").exists(false).and("replyTo").exists(false));

        // 如果 commentId 不为空，则查询小于 commentId 的评论
        if (commentId != null) {
            Comment lastComment = mongoTemplate.findOne(Query.query(Criteria.where("id").is(commentId)), Comment.class);
            if (lastComment == null) {
                throw new BadRequestException("评论id不存在");
            }
            query.addCriteria(Criteria.where("commentTime").lt(lastComment.getCommentTime()));
        }

        // 设置排序（按评论时间倒序）
        query.with(Sort.by(Sort.Direction.DESC, "commentTime"));

        // 设置分页
        query.limit(COMMENT_SIZE);

        // 执行查询
        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        if (comments.isEmpty()) {
            return null;
        }
        return generateCommentsResp(comments, true);
    }

    @Override
    public List<CommentRespDto> getReplies(String parentId, String commentId) {
        Query query = Query.query(Criteria.where("parentId").is(parentId));

        // 如果 commentId 不为空，则查询小于 commentId 的评论
        if (commentId != null) {
            Comment lastComment = mongoTemplate.findOne(Query.query(Criteria.where("id").is(commentId)), Comment.class);
            if (lastComment == null) {
                throw new BadRequestException("评论id不存在");
            }
            query.addCriteria(Criteria.where("commentTime").gt(lastComment.getCommentTime()));
        }

        // 设置排序（按评论时间升序）
        query.with(Sort.by(Sort.Direction.ASC, "commentTime"));

        // 设置分页
        query.limit(COMMENT_SIZE);

        // 执行查询
        List<Comment> replies = mongoTemplate.find(query, Comment.class);
        if (replies.isEmpty()) {
            return null;
        }
        return generateCommentsResp(replies, false);
    }

    @Override
    public Integer getCommentCount(String videoTitle) {
        return (Integer) redisTemplate.opsForHash().get("commentCount", videoTitle);
    }

    @Override
    public void addComment(CommentReqDto commentReqDto) {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment newComment = new Comment();
        String videoId = commentReqDto.getVideoId();
        newComment.setVideoId(videoId);
        newComment.setCommentText(commentReqDto.getCommentText());
        newComment.setUsername(authUser.getUser().getUsername());
        newComment.setCommentTime(System.currentTimeMillis());
        mongoTemplate.insert(newComment);
        String commentCopyId = (String) redisTemplate.opsForHash().get("currentCommentCopyIdMap", videoId);
        String currentHotCommentKey = "hotComments:" + videoId + ":copyId:" + commentCopyId;
        if (!redisTemplate.hasKey(currentHotCommentKey)) {
            redisTemplate.opsForZSet().add(currentHotCommentKey, newComment.getId(), -System.currentTimeMillis());
            redisTemplate.expire(currentHotCommentKey, 4, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForZSet().add(currentHotCommentKey, newComment.getId(), -System.currentTimeMillis());
        }
        redisUtil.getBloomFilter("commentBloomFilter", 1000000, 0.03).add(newComment.getId());

        redisTemplate.opsForHash().increment("commentCount", videoId, 1);
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
        redisTemplate.opsForZSet().add("activeVideos", videoId, System.currentTimeMillis());
        videoService.changeRecommendModel(videoCache.getTags(), authUser.getUser().getUsername(), 4);
    }

    @Override
    public void addReply(ReplyReqDto replyReqDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        User user = userMapper.selectOne(queryWrapper.eq("nickname", replyReqDto.getReplyTo()));
        if (user == null) {
            throw new BadRequestException("回复对象不存在");
        }
        Comment parentComment = mongoTemplate.findById(replyReqDto.getParentId(), Comment.class);
        if (parentComment == null) {
            throw new BadRequestException("父评论不存在");
        }

        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment newComment = new Comment();
        String videoId = replyReqDto.getVideoId();
        newComment.setVideoId(videoId);
        newComment.setCommentText(replyReqDto.getCommentText());
        newComment.setParentId(replyReqDto.getParentId());
        newComment.setReplyTo(replyReqDto.getReplyTo());
        newComment.setUsername(authUser.getUser().getUsername());
        newComment.setCommentTime(System.currentTimeMillis());
        mongoTemplate.save(newComment);
        redisUtil.getBloomFilter("commentBloomFilter", 1000000, 0.03).add(newComment.getId());
        redisTemplate.opsForHash().increment("commentCount", videoId, 1);
        redisTemplate.opsForZSet().add("activeVideos", videoId, System.currentTimeMillis());
    }

    @Override
    public void likeComment(String commentId, Boolean isParent) {
        Comment comment = redisUtil.getCommentFromRedis(commentId);
        comment.setLikeCount(comment.getLikeCount() + 1);
        mongoTemplate.save(comment);
        // 在redis中更新缓存
        redisTemplate.opsForValue().set("commentCache:" + commentId, comment, 2, TimeUnit.HOURS);
        // 在redis中缓存用户喜欢的评论
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redisTemplate.opsForSet().add("likeComments:" + authUser.getUser().getUsername(), commentId);
    }

    @Override
    public void dislikeComment(String commentId, Boolean isParent) {
        // 给文档的likeCount字段减一
        Comment comment = mongoTemplate.findById(commentId, Comment.class);
        if (comment == null) {
            throw new BadRequestException("评论不存在");
        }
        comment.setLikeCount(comment.getLikeCount() - 1);
        mongoTemplate.save(comment);
        if (isParent) {
            redisTemplate.opsForValue().set("hotComments:" + commentId, comment, 2, TimeUnit.HOURS);
        }
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redisTemplate.opsForSet().remove("likeComments:" + authUser.getUser().getUsername(), commentId);
    }


    public List<CommentRespDto> generateCommentsResp(List<Comment> comments, Boolean isParent) {
        AuthUser authUser = AuthConfig.getAuthUser();
        String username;
        if (authUser != null) {
            username = authUser.getUser().getUsername();
        } else {
            username = null;
        }
        return comments.stream().map(comment -> {
            CommentRespDto commentRespDto = new CommentRespDto();
            commentRespDto.setId(comment.getId());
            commentRespDto.setVideoId(comment.getVideoId());
            commentRespDto.setAvatar(cosUtil.getAvatarUrl(comment.getUsername()));
            commentRespDto.setNickname(userMapper.findNicknameByUsername(comment.getUsername()));
            commentRespDto.setCommentText(comment.getCommentText());
            commentRespDto.setLikeCount(comment.getLikeCount());
            if (username != null) {
                commentRespDto.setIsLike(redisTemplate.opsForSet().isMember("likeComments:" + username, comment.getId()));
            } else {
                commentRespDto.setIsLike(false);
            }
            commentRespDto.setCommentTime(CommonUtil.formatTimeString(comment.getCommentTime()));
            commentRespDto.setReplyTo(comment.getReplyTo());
            if (isParent) {
                commentRespDto.setReplyCount((int) mongoTemplate.count(Query.query(Criteria.where("parentId").is(comment.getId())), Comment.class));
            }
            return commentRespDto;
        }).toList();
    }
}
