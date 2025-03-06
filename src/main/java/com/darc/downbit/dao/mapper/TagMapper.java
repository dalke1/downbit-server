package com.darc.downbit.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.darc.downbit.dao.entity.Tag;

import java.util.List;

/**
 * @author 16216
 * @description 针对表【tag(标签表)】的数据库操作Mapper
 * @createDate 2025-03-05 18:59:29
 * @Entity com.darc.downbit.dao.entity.Tag
 */
public interface TagMapper extends BaseMapper<Tag> {

    List<Integer> getTagIdsByTagNames(List<String> tagNames);
}




