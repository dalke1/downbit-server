package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.FavoriteReqDto;
import com.darc.downbit.service.FavoriteService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-02:46:19
 * @description
 */
@RestController
@RequestMapping("/api/favorite")
@Validated
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @PutMapping("/add_favorite")
    public Object addFavorite(@RequestParam("favoriteName") @NotNull @NotBlank String favoriteName) {
        if (favoriteService.addFavorite(favoriteName)) {
            return RestResp.ok();
        }
        return RestResp.internalServerError("添加收藏夹失败");
    }

    @DeleteMapping("/remove_favorite")
    public Object removeFavorite(@RequestParam("favoriteName") @NotNull @NotBlank String favoriteName) {
        if (favoriteService.removeFavorite(favoriteName)) {
            return RestResp.ok();
        }
        return RestResp.internalServerError("删除收藏夹失败");
    }

    @PutMapping("/add_video_to_favorite")
    public Object addVideoToFavorite(@RequestBody @Validated FavoriteReqDto favoriteReqDto) {
        if (favoriteService.addVideoToFavorite(favoriteReqDto)) {
            return RestResp.ok();
        }
        return RestResp.internalServerError("添加视频到收藏夹失败");
    }

    @DeleteMapping("/remove_video_from_favorite")
    public Object removeVideoFromFavorite(@RequestBody @Validated FavoriteReqDto favoriteReqDto) {
        if (favoriteService.removeVideoFromFavorite(favoriteReqDto)) {
            return RestResp.ok();
        }
        return RestResp.internalServerError("从收藏夹中移除视频失败");
    }

    @GetMapping("/get_favorites")
    public Object getFavorites() {
        return RestResp.ok(favoriteService.getFavorites());
    }

    @GetMapping("/get_favorite_videos")
    public Object getFavoriteVideos(@RequestParam("favoriteName") @NotNull @NotBlank String favoriteName) {
        return RestResp.ok(favoriteService.getFavoriteVideos(favoriteName));
    }
}
