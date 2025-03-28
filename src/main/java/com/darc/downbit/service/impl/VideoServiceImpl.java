package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.dto.rep.SearchVideoReq;
import com.darc.downbit.common.dto.rep.UpdateVideoDto;
import com.darc.downbit.common.dto.rep.UploadVideoDto;
import com.darc.downbit.common.dto.rep.VideoReqDto;
import com.darc.downbit.common.dto.resp.*;
import com.darc.downbit.common.exception.*;
import com.darc.downbit.config.auth.AuthConfig;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.*;
import com.darc.downbit.dao.mapper.*;
import com.darc.downbit.service.RecommendService;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CommonUtil;
import com.darc.downbit.util.CosUtil;
import com.darc.downbit.util.HotRankUtil;
import com.darc.downbit.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private HotRankUtil hotRankUtil;

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

    @Resource
    private MongoTemplate mongoTemplate;

    @Value("${downbit.recommend}")
    private String isRecommend;

    public static final int MAX_PAGE_VIDEOS = 12;

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
        video.setDuration(uploadVideoDto.getDuration());
        video.setVideoFormat(uploadVideoDto.getFormat());
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
        String currentGlobalCopyId = (String) redisTemplate.opsForHash().get("currentCopyIdMap", "全站");
        redisTemplate.opsForZSet().add("hotVideos:全站:" + currentGlobalCopyId, String.valueOf(video.getVideoId()), -System.currentTimeMillis());

        // 按标签添加
        for (String tag : uploadVideoDto.getTags()) {
            String currentTagCopyId = (String) redisTemplate.opsForHash().get("currentCopyIdMap", tag);
            String currentTagHotVideoKey = "hotVideos:标签:" + tag + ":" + currentTagCopyId;
            if (!redisTemplate.hasKey(currentTagHotVideoKey)) {
                redisTemplate.opsForZSet().add(currentTagHotVideoKey, String.valueOf(video.getVideoId()), -System.currentTimeMillis());
                redisTemplate.expire(currentTagHotVideoKey, 3, TimeUnit.HOURS);
            } else {
                redisTemplate.opsForZSet().add(currentTagHotVideoKey, String.valueOf(video.getVideoId()), -System.currentTimeMillis());
            }
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
                .map(tag -> new TagRespDto(String.valueOf(tag.getTagId()), tag.getTagName()))
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
        if ("no".equals(isRecommend)) {
            return generateRandomVideo(username, userId, false);
        }
        String userModelKey = "userModel:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(userModelKey))) {
            return generateRandomVideo(username, userId, false);
        }
        String recommendKey = "recommend:" + username;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(recommendKey))) {
            recommendService.getRecommendVideos(username, userId);
            return generateRandomVideo(username, userId, false);
        }
        //从zset中获取获取评分最高的,并删除
        Set<String> recommend = redisTemplate.opsForZSet().reverseRange(recommendKey, 0, -1);
        if (recommend == null || recommend.isEmpty()) {
            return generateRandomVideo(username, userId, false);
        }
        String videoId = recommend.stream().findFirst().orElse(null);
        RBloomFilter<Object> bloomFilter = redisUtil.getBloomFilter("bloomFilter:" + username, 10000, 0.01);
        bloomFilter.add(videoId);
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

    @Override
    public List<TagRespDto> getInterestTags() {
        AuthUser authUser = AuthConfig.getAuthUser();
        AtomicInteger index = new AtomicInteger(2);
        List<Tag> tags = tagMapper.selectList(new QueryWrapper<>()).stream().toList();
        if (authUser == null) {
            // 查询数据库中所有的标签,随机返回几个标签
            return tags.stream()
                    .limit(4)
                    .map(tag -> new TagRespDto(String.valueOf(index.getAndIncrement()), tag.getTagName()))
                    .toList();
        } else {
            String userModelKey = "userModel:" + authUser.getUser().getUsername();
            if (!redisTemplate.hasKey(userModelKey)) {
                return tags.stream()
                        .limit(4)
                        .map(tag -> new TagRespDto(String.valueOf(index.getAndIncrement()), tag.getTagName()))
                        .toList();
            }
            Map<Object, Object> userModel = redisTemplate.opsForHash().entries(userModelKey);
            // 前端tabs数组的固定有前两个tab 热门->hot 和 最新->new,所以标签tab的code也就是tabs数组的索引从2开始
            List<TagRespDto> tagRespDtoList = userModel.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(
                            Double.parseDouble(String.valueOf(e2.getValue())),
                            Double.parseDouble(String.valueOf(e1.getValue()))
                    ))
                    .limit(5)
                    .map(entry -> String.valueOf(entry.getKey()))
                    .map(tagName -> new TagRespDto(String.valueOf(index.getAndIncrement()), tagName))
                    .toList();
            int size = tagRespDtoList.size();
            if (size < 4) {
                List<TagRespDto> supplement = new ArrayList<>(tags.stream()
                        .limit(4 - size)
                        .map(tag -> new TagRespDto(String.valueOf(index.getAndIncrement()), tag.getTagName()))
                        .toList());
                supplement.addAll(tagRespDtoList);
                return supplement;
            }
            return tagRespDtoList;
        }
    }

    @Override
    public List<VideoRespDto> getNewVideos(String videoId) {
        if (videoId == null) {
            return videoMapper.getVideosIdOrderByUploadTime(MAX_PAGE_VIDEOS).stream()
                    .map(newVideoId -> produceVideo(null, null, String.valueOf(newVideoId), true))
                    .toList();
        } else {
            return videoMapper.getVideosIdByCursorAndUploadTime(Integer.parseInt(videoId), MAX_PAGE_VIDEOS).stream()
                    .map(newVideoId -> produceVideo(null, null, String.valueOf(newVideoId), true))
                    .toList();
        }
    }

    @Override
    public VideoPage getHotVideos(String copyId, Integer index) {
        String currentGlobalCopyId = (String) redisTemplate.opsForHash().get("currentCopyIdMap", "全站");
        String currentGlobalHotKey = "hotVideos:全站:" + currentGlobalCopyId;
        String previousGlobalHotKey = "hotVideos:全站:" + copyId;
        return getHotRankVideoPage(previousGlobalHotKey, currentGlobalHotKey, copyId, currentGlobalCopyId, index);
    }

    @Override
    public VideoPage getHotVideosByTag(String tag, String copyId, Integer index) {
        String currentTagCopyId = (String) redisTemplate.opsForHash().get("currentCopyIdMap", tag);
        String currentTagHotKey = "hotVideos:标签:" + tag + ":" + currentTagCopyId;
        String previousTagHotKey = "hotVideos:标签:" + tag + ":" + copyId;
        return getHotRankVideoPage(previousTagHotKey, currentTagHotKey, copyId, currentTagCopyId, index);
    }

    @Override
    public VideoRespDto getVideoInfo(String videoId) {
        AuthUser authUser = AuthConfig.getAuthUser();
        if (authUser == null) {
            return produceVideo(null, null, videoId, true);
        }
        User user = authUser.getUser();
        return produceVideo(user.getUsername(), user.getUserId(), videoId, false);
    }

    @Override
    public List<VideoRespDto> getRelatedVideoList(String videoId) {
        String relatedKey = "related:" + videoId;
        Set<String> relatedVideoSet;
        if (!redisTemplate.hasKey(relatedKey)) {
            recommendService.getRelatedVideos(videoId);
        }
        relatedVideoSet = redisTemplate.opsForZSet().reverseRange(relatedKey, 0, 9);
        if (relatedVideoSet == null || relatedVideoSet.isEmpty()) {
            return List.of();
        } else {
            return relatedVideoSet.stream()
                    .map(id -> produceVideo(null, null, id, true))
                    .toList();
        }
    }

    @Override
    public UpdateInfo getVideoForUpdate(String videoId) {
        if (videoId == null) {
            throw new BadRequestException("没有视频ID");
        }
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
        if (videoCache == null) {
            throw new BadRequestException("没有该视频");
        }
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.setVideoDescription(videoCache.getVideoDescription());
        updateInfo.setTitle(videoCache.getVideoTitle());
        List<TagRespDto> tagRespDtoList = videoCache.getTags().stream()
                .map(tagName -> {
                    Tag tag = tagMapper.selectOne(new QueryWrapper<Tag>().eq("tag_name", tagName));
                    return new TagRespDto(String.valueOf(tag.getTagId()), tagName);
                }).toList();
        updateInfo.setTags(tagRespDtoList);
        return updateInfo;
    }

    @Override
    @Transactional
    public void updateVideo(UpdateVideoDto updateVideoDto) {
        String videoId = updateVideoDto.getVideoId();
        Integer id = Integer.parseInt(videoId);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<Video>().eq("video_id", id);
        Video video = videoMapper.selectOne(queryWrapper);
        if (updateVideoDto.getVideoTitle() != null) {
            video.setVideoTitle(updateVideoDto.getVideoTitle());
        }

        if (updateVideoDto.getVideoDescription() != null) {
            video.setVideoDescription(updateVideoDto.getVideoDescription());
        }
        videoMapper.updateById(video);
        if (updateVideoDto.getTags() != null) {
            videoTagMapper.delete(new QueryWrapper<VideoTag>().eq("video_id", id));
            // 标签Id列表
            List<Integer> tagIdList = tagMapper.getTagIdsByTagNames(updateVideoDto.getTags());
            // 插入视频标签关系
            tagIdList.forEach(tagId -> {
                VideoTag videoTag = new VideoTag();
                videoTag.setVideoId(video.getVideoId());
                videoTag.setTagId(tagId);
                videoTagMapper.insert(videoTag);
            });
        }

        mongoTemplate.remove(Query.query(Criteria.where("_id").is(videoId)), VideoCache.class);
        if (redisTemplate.hasKey("videoCache:" + videoId)) {
            redisTemplate.delete("videoCache:" + videoId);
        }
    }

    @Override
    @Transactional
    public void deleteVideo(String videoId) {
        AuthUser authUser = AuthConfig.getAuthUser();
        if (authUser == null) {
            throw new NoPermissionException("没有登录");
        }
        User user = authUser.getUser();
        String username = user.getUsername();
        RBloomFilter<Object> videoBloomFilter = redisUtil.getBloomFilter("videoBloomFilter", 1000000, 0.03);
        if (!videoBloomFilter.contains(videoId)) {
            throw new BadRequestException("没有该视频");
        }
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(videoId);
        Integer id = Integer.parseInt(videoId);
        String videoFileName = videoMapper.getFileNameByVideoId(id);
        String coverFileName = videoMapper.getCoverByVideoId(id);


        if (imgMapper.deleteImgByVideoId(id) < 1) {
            throw new DatabaseException("删除封面图片失败,视频id:" + videoId);
        }
        favoriteVideoMapper.delete(new QueryWrapper<FavoriteVideo>().eq("video_id", id));
        if (videoTagMapper.deleteVideoTagByVideoId(id) < 1) {
            throw new DatabaseException("删除视频标签失败,视频id:" + videoId);
        }
        Video video = videoMapper.selectOne(new QueryWrapper<Video>().eq("video_id", id));
        if (videoMapper.delete(new QueryWrapper<Video>().eq("video_id", id)) < 1) {
            throw new DatabaseException("删除视频失败,视频id:" + videoId);
        }
        if (fileMapper.deleteCoverFile(video.getCoverFileId()) < 1) {
            throw new DatabaseException("删除封面文件失败,封面文件id:" + videoId);
        }
        if (fileMapper.deleteVideoFile(video.getFileId()) < 1) {
            throw new DatabaseException("删除视频文件失败,视频文件id:" + videoId);
        }

        mongoTemplate.remove(Query.query(Criteria.where("_id").is(videoId)), VideoCache.class);
        mongoTemplate.remove(Query.query(Criteria.where("videoId").is(videoId)), Comment.class);
        redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                    redisTemplate.delete("videoCache:" + videoId);
                    redisTemplate.opsForHash().delete("likeCount", videoId);
                    redisTemplate.opsForHash().delete("favoriteCount", videoId);
                    redisTemplate.opsForHash().delete("commentCount", videoId);
                    redisTemplate.opsForHash().delete("watchCount", videoId);
                    redisTemplate.opsForZSet().remove("activeVideos", videoId);
                    redisTemplate.opsForZSet().remove("history:" + username, videoId);
                    String currentGlobalCopyId = (String) redisTemplate.opsForHash().get("currentCopyIdMap", "全站");
                    String currentGlobalHotKey = "hotVideos:全站:" + currentGlobalCopyId;
                    redisTemplate.opsForZSet().remove(currentGlobalHotKey, videoId);
                    redisTemplate.opsForHash().delete("currentCommentCopyIdMap", videoId);
                    videoCache.getTags().forEach(tag -> {
                        String currentTagCopyId = (String) redisTemplate.opsForHash().get("currentCopyIdMap", tag);
                        String currentTagHotKey = "hotVideos:标签:" + tag + ":" + currentTagCopyId;
                        redisTemplate.opsForZSet().remove(currentTagHotKey, videoId);
                    });
                    redisTemplate.opsForSet().remove("like:" + username, videoId);
                    return null;
                }
        );

        cosUtil.deleteFiles(List.of("video/" + username + "/" + videoFileName, "cover/" + username + "/" + coverFileName));
    }

    @Override
    public SearchVideoResp searchVideos(SearchVideoReq dto) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Video::getVideoTitle, dto.getQuery())
                .or()
                .like(Video::getVideoDescription, dto.getQuery());

        // 时间排序
        if ("date".equals(dto.getSortBy())) {
            wrapper.orderByDesc(Video::getUploadTime);
        } else {
            // 热度排序，可以使用权重计算
            wrapper.orderByDesc(Video::getLikeCount)
                    .orderByDesc(Video::getWatchCount);
        }

        // 执行分页查询
        Page<Video> page = new Page<>(dto.getPage(), dto.getSize());
        Page<Video> videoPage = videoMapper.selectPage(page, wrapper);

        List<VideoRespDto> videoRespDtoList = videoPage.getRecords().stream()
                .map(video -> produceVideo(null, null, String.valueOf(video.getVideoId()), true))
                .toList();

        return new SearchVideoResp(videoRespDtoList, videoPage.getTotal());
    }

    public VideoPage getHotRankVideoPage(String previousHotKey, String currentHotKey, String copyId, String currentCopyId, Integer index) {
        List<String> hotVideoIdList;
        boolean isFirstPage = true;
        if (copyId == null) {
            hotVideoIdList = hotRankUtil.getHotVideos(0, MAX_PAGE_VIDEOS - 1, currentHotKey);
            if (hotVideoIdList.isEmpty()) {
                return null;
            }
        } else if (index == null) {
            throw new BadRequestException("分页获取热度排行榜没有索引");
        } else {
            if (!redisTemplate.hasKey(previousHotKey)) {
                throw new RefreshPage("用户传递的copyId已经过期,让用户刷新页面重新获取copyId");
            }
            hotVideoIdList = hotRankUtil.getHotVideos(index, index + MAX_PAGE_VIDEOS - 1, previousHotKey);
            if (hotVideoIdList.isEmpty()) {
                return null;
            }
            isFirstPage = false;
        }
        List<VideoRespDto> videoRespDtoList = hotVideoIdList.stream()
                .map(videoId -> produceVideo(null, null, videoId, true))
                .toList();
        return isFirstPage ? new VideoPage(videoRespDtoList, currentCopyId) : new VideoPage(videoRespDtoList, copyId);
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
        String avatarUrl = cosUtil.getAvatarUrl(videoCache.getUploader());
        return new VideoRespDto(
                videoCache.getVideoId(),
                videoTitle,
                coverUrl,
                videoUrl,
                videoCache.getVideoFormat(),
                likeCount,
                commentCount,
                watchCount,
                favoriteCount,
                uploader,
                CommonUtil.formatTimeString(videoCache.getUploadTime()),
                isLike,
                isFavorite,
                videoCache.getTags(),
                videoCache.getDuration(),
                avatarUrl,
                videoCache.getVideoDescription()
        );
    }

}




