package com.darc.downbit.common.dto.rep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-00:19:06
 * @description
 */
@Data
@AllArgsConstructor
public class FavoriteReqDto {
    @NotBlank(message = "收藏夹名称不能为空格")
    @NotNull(message = "收藏夹名称不能为空")
    private String favoriteName;
    @NotBlank(message = "视频ID不能为空格")
    @NotNull(message = "视频ID不能为空")
    private String videoId;
}
