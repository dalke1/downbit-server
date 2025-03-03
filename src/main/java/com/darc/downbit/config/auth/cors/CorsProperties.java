package com.darc.downbit.config.auth.cors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @date 2024/7/22-2:40:50
 * @description 跨域配置属性
 */
@ConfigurationProperties(prefix = "downbit.cors")
@Data
public class CorsProperties {
    /**
     * 允许跨域的域名
     */
    private List<String> allowOrigins;
}
