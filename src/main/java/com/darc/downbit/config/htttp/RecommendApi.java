package com.darc.downbit.config.htttp;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/27-12:29:45
 * @description
 */
public interface RecommendApi {
    @PostExchange(value = "http://localhost:8000/recommend/", contentType = "application/json", accept = "application/json")
    Mono<String> getRecommend(@RequestBody String body);
}
