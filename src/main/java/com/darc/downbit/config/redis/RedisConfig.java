package com.darc.downbit.config.redis;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/14-23:08:30
 * @description
 */
@SpringBootConfiguration
public class RedisConfig {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        //自定义设置序列化机制,把对象转为json
        // 使用 StringRedisSerializer 序列化键
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用 GenericJackson2JsonRedisSerializer 序列化值
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        stringRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return stringRedisTemplate;
    }
}
