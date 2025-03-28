package com.darc.downbit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author darc
 */
@EnableAsync
@EnableKafka
@EnableScheduling
@EnableMethodSecurity
@MapperScan("com.darc.downbit.dao.mapper")
@SpringBootApplication
public class DownBitServerApplication {

    // 定义一个名为main的静态方法，这是Java程序的入口点
    public static void main(String[] args) {
        SpringApplication.run(DownBitServerApplication.class, args);
    }

}


