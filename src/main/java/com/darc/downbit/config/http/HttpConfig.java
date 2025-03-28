package com.darc.downbit.config.http;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-20:21:28
 * @description
 */
@Data
@SpringBootConfiguration
public class HttpConfig {

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(@Value("${downbit.aliyun.auth}") String auth) {
        WebClient webClient = WebClient.builder()
                .defaultHeader("Authorization", "APPCODE " + auth)
                .codecs(clientCodecConfigurer -> clientCodecConfigurer
                        .defaultCodecs()
                        .maxInMemorySize(256 * 1024 * 1024))
                .build();
        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
    }

    /**
     * 天气功能
     *
     * @return WeatherInterface
     */
    @Bean
    SmsApi smsApi(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(SmsApi.class);
    }

    @Bean
    RecommendApi recommendApi(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(RecommendApi.class);
    }
}
