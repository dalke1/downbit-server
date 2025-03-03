package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.dao.entity.FavoriteVideo;
import com.darc.downbit.dao.mapper.FavoriteVideoMapper;
import com.darc.downbit.service.FavoriteVideoService;
import org.springframework.stereotype.Service;

/**
 * @author 16216
 * @description 针对表【favorite_video(收藏夹视频关系表)】的数据库操作Service实现
 * @createDate 2024-12-26 23:59:02
 */
@Service
public class FavoriteVideoServiceImpl extends ServiceImpl<FavoriteVideoMapper, FavoriteVideo>
        implements FavoriteVideoService {

}




