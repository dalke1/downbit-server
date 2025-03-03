package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.dao.entity.File;
import com.darc.downbit.dao.mapper.FileMapper;
import com.darc.downbit.service.FileService;
import org.springframework.stereotype.Service;

/**
 * @author 16216
 * @description 针对表【file(上传到腾讯云cos的文件表)】的数据库操作Service实现
 * @createDate 2024-12-20 01:23:44
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements FileService {

}




