package com.darc.downbit.config.htttp;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/17-21:15:18
 * @description
 */
public interface SmsApi {
    @PostExchange(value = "https://gyytz.market.alicloudapi.com/sms/smsSend", accept = "application/json")
    Mono<String> sendSms(@RequestParam("mobile") String mobile,
                         @RequestParam("templateId") String templateId,
                         @RequestParam("smsSignId") String smsSignId,
                         @RequestParam("param") String param);
}
