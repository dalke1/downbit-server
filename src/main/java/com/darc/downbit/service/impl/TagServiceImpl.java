package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.dao.entity.Tag;
import com.darc.downbit.dao.mapper.TagMapper;
import com.darc.downbit.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author 16216
 * @description 针对表【tag(标签表)】的数据库操作Service实现
 * @createDate 2025-03-05 18:59:29
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

}




