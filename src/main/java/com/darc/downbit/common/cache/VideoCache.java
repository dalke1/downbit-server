package com.darc.downbit.common.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/20-18:20:18
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "video")
public class VideoCache {
    @Id
    private String videoId;
    private String uploader;
    private String videoTitle;
    private String fileName;
    private String coverFileName;
    private String videoFormat;
    private String videoDescription;
    private List<String> tags;
    private Long uploadTime;
    private String duration;
}
