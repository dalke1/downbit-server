package com.darc.downbit.service;

import com.darc.downbit.common.exception.NoMoreRecommendException;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/23-22:01:55
 * @description
 */
public interface RecommendService {
    void getRecommendVideos(String username, Integer userId) throws NoMoreRecommendException;

    void getRelatedVideos(String videoId) throws NoMoreRecommendException;
}
