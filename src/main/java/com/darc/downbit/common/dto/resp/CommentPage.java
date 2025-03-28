package com.darc.downbit.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/3/20-01:31:10
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentPage {
    private List<CommentRespDto> comments;
    private String copyId;
}
