package com.darc.downbit.service.api;

import com.darc.downbit.config.htttp.WeatherApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-20:35:05
 * @description
 */
@Service
public class WeatherApiService {
    @Resource
    WeatherApi weatherApi;

    public Mono<String> getWeatherByCityName(String cityName) {
        return weatherApi.getWeather40day(cityName);
    }
}
