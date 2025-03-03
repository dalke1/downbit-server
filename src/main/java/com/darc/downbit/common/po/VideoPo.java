package com.darc.downbit.common.po;

import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/21-02:54:23
 * @description 视频表和文件表的联合查询结果, 对应视频表的视频标题和文件表的文件名
 */
@Data
public class VideoPo {
    private String uploader;
    private String videoTitle;
    private String fileName;
}
