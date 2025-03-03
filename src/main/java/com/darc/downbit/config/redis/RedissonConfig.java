package com.darc.downbit.config.redis;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/20-17:33:52
 * @description
 */
@Data
@SpringBootConfiguration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonConfig {
    private String host;
    private String port;
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port).setPassword(password);
        return Redisson.create(config);
    }
}
