package com.darc.downbit.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/11-18:41:44
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comment")
public class Comment {
    @Id
    private String id;
    private String username;
    private Long commentTime;
    private String commentText;
    private String videoId;
    private Integer likeCount = 0;
    private String parentId;
    private String replyTo;
}
