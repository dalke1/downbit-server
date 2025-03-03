package com.darc.downbit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author darc
 */
@EnableAsync
@EnableKafka
@EnableMethodSecurity
@MapperScan("com.darc.downbit.dao.mapper")
@SpringBootApplication
public class DownBitServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DownBitServerApplication.class, args);
    }

}


