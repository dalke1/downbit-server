package com.darc.downbit.config.auth.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/2-23:29:15
 * @description
 */
@ConfigurationProperties(prefix = "downbit.filter")
@Data
public class FilterProperties {
    List<String> passPath;
    List<String> guestPath;
}
