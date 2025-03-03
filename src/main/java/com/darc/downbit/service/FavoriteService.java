package com.darc.downbit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.darc.downbit.common.dto.rep.FavoriteReqDto;
import com.darc.downbit.common.dto.resp.CoverRespDto;
import com.darc.downbit.dao.entity.Favorite;

import java.util.List;

/**
 * @author 16216
 * @description 针对表【favorite】的数据库操作Service
 * @createDate 2024-12-26 23:58:40
 */
public interface FavoriteService extends IService<Favorite> {
    /**
     * 新增收藏夹
     *
     * @return 新增结果
     */
    boolean addFavorite(String favoriteName);

    /**
     * 删除收藏夹
     *
     * @return 删除结果
     */

    boolean removeFavorite(String favoriteName);

    /**
     * 添加视频到收藏夹
     *
     * @return 添加结果
     */
    boolean addVideoToFavorite(FavoriteReqDto favoriteReqDto);

    /**
     * 从收藏夹中移除视频
     *
     * @return 移除结果
     */
    boolean removeVideoFromFavorite(FavoriteReqDto favoriteReqDto);

    /**
     * 获取用户收藏夹
     *
     * @return 用户收藏夹
     */
    List<String> getFavorites();

    /**
     * 获取用户收藏夹视频
     *
     * @return 用户收藏夹视频
     */
    List<CoverRespDto> getFavoriteVideos(String favoriteName);
}
