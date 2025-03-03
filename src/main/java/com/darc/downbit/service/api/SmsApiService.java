package com.darc.downbit.service.api;

import com.darc.downbit.config.htttp.SmsApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-21:22:31
 * @description
 */

@Service
public class SmsApiService {

    @Resource
    SmsApi smsApi;

    public Mono<String> sendSms(String phone) {

        return smsApi.sendSms(phone,
                "908e94ccf08b4476ba6c876d13f084ad",
                "2e65b1bb3d054466b82f0c9d125465e2",
                "**code**:456789,**minute**:5");
    }
}
