package com.darc.downbit.controller.front;

import com.darc.downbit.service.api.SmsApiService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-21:55:34
 * @description
 */
@RestController
public class SmsController {
    @Resource
    SmsApiService smsService;

    @PostMapping("/api/sendSms")
    public Mono<String> sendSms() {
        return smsService.sendSms("13829042381");
    }
}
