package com.darc.downbit.config.htttp;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-20:22:19
 * @description
 */


public interface WeatherApi {
    @GetExchange(value = "https://weather110.market.alicloudapi.com/getWeather_40day", accept = "application/json")
    Mono<String> getWeather40day(@RequestParam("cityName") String cityName);
}
