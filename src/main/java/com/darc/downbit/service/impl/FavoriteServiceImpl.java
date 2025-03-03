package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.common.cache.VideoCache;
import com.darc.downbit.common.dto.rep.FavoriteReqDto;
import com.darc.downbit.common.dto.resp.CoverRespDto;
import com.darc.downbit.config.auth.AuthConfig;
import com.darc.downbit.dao.entity.Favorite;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.mapper.FavoriteMapper;
import com.darc.downbit.dao.mapper.FavoriteVideoMapper;
import com.darc.downbit.service.FavoriteService;
import com.darc.downbit.service.VideoService;
import com.darc.downbit.util.CosUtil;
import com.darc.downbit.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 16216
 * @description 针对表【favorite】的数据库操作Service实现
 * @createDate 2024-12-26 23:58:40
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
        implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private FavoriteVideoMapper favoriteVideoMapper;

    @Resource
    private CosUtil cosUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private VideoService videoService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean addFavorite(String favoriteName) {
        Integer userId = AuthConfig.getAuthUser().getUser().getUserId();
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setFavoriteName(favoriteName);
        return favoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeFavorite(String favoriteName) {
        Integer userId = AuthConfig.getAuthUser().getUser().getUserId();
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("favorite_name", favoriteName).eq("user_id", userId);
        return favoriteMapper.delete(queryWrapper) > 0;
    }

    @Override
    public boolean addVideoToFavorite(FavoriteReqDto favoriteReqDto) {
        User user = AuthConfig.getAuthUser().getUser();
        Integer userId = user.getUserId();
        String favoriteName = favoriteReqDto.getFavoriteName();
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(favoriteReqDto.getVideoId());
        videoService.changeRecommendModel(videoCache.getTags(), user.getUsername(), 3);
        redisTemplate.opsForHash().increment("favoriteCount", favoriteReqDto.getVideoId(), 1);
        redisTemplate.opsForZSet().add("activeVideos", favoriteReqDto.getVideoId(), System.currentTimeMillis());
        return favoriteVideoMapper.insertByFavoriteNameAndVideoTitle(userId, favoriteName, Integer.valueOf(favoriteReqDto.getVideoId())) > 0;
    }

    @Override
    public boolean removeVideoFromFavorite(FavoriteReqDto favoriteReqDto) {
        User user = AuthConfig.getAuthUser().getUser();
        Integer userId = user.getUserId();
        String favoriteName = favoriteReqDto.getFavoriteName();
        VideoCache videoCache = redisUtil.getVideoCacheFromRedis(favoriteReqDto.getVideoId());
        videoService.changeRecommendModel(videoCache.getTags(), user.getUsername(), -3);
        redisTemplate.opsForHash().increment("favoriteCount", favoriteReqDto.getVideoId(), -1);
        return favoriteVideoMapper.deleteByFavoriteNameAndVideoTitle(userId, favoriteName, Integer.valueOf(favoriteReqDto.getVideoId())) > 0;
    }

    @Override
    public List<String> getFavorites() {
        Integer userId = AuthConfig.getAuthUser().getUser().getUserId();
        return favoriteMapper.getFavoriteNamesByUserId(userId);
    }

    @Override
    public List<CoverRespDto> getFavoriteVideos(String favoriteName) {
        User user = AuthConfig.getAuthUser().getUser();
        Integer userId = user.getUserId();
        String username = user.getUsername();
        return favoriteVideoMapper.getVideosIdByFavoriteName(userId, favoriteName).stream()
                .map(videoId -> {
                    VideoCache videoCache = redisUtil.getVideoCacheFromRedis(String.valueOf(videoId));
                    return new CoverRespDto(
                            videoCache.getVideoId(),
                            videoCache.getVideoTitle(),
                            cosUtil.getCoverUrl(username, videoCache.getCoverFileName()));
                })
                .toList();
    }
}




