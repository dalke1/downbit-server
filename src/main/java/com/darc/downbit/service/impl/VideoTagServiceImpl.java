package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.dao.entity.VideoTag;
import com.darc.downbit.dao.mapper.VideoTagMapper;
import com.darc.downbit.service.VideoTagService;
import org.springframework.stereotype.Service;

/**
 * @author 16216
 * @description 针对表【video_tag(视频标签关联表)】的数据库操作Service实现
 * @createDate 2025-03-05 18:59:47
 */
@Service
public class VideoTagServiceImpl extends ServiceImpl<VideoTagMapper, VideoTag>
        implements VideoTagService {

}




