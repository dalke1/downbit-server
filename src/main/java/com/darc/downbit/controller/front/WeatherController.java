package com.darc.downbit.controller.front;

import com.darc.downbit.service.api.WeatherApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-20:39:24
 * @description
 */
@RestController
public class WeatherController {
    @Autowired
    WeatherApiService weatherService;

    @GetMapping("/api/getWeather_40day")
    public Mono<String> getWeather40day() {
        return weatherService.getWeatherByCityName("潮州");
    }
}
