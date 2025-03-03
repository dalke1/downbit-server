package com.darc.downbit.config.async;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author darc
 * @version 0.1
 * @createDate 2025/2/23-21:56:36
 * @description
 */
@SpringBootConfiguration
public class AsyncThreadPoolConfig {
    private static final int MAX_POOL_SIZE = 50;
    private static final int CORE_POOL_SIZE = 20;
    private static final int TASK_NUM = 200;
    private static final int ACTIVE_TIME = 60;

    @Bean("asyncTaskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor asyncTaskExecutor = new ThreadPoolTaskExecutor();
        asyncTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        asyncTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        asyncTaskExecutor.setQueueCapacity(TASK_NUM);
        asyncTaskExecutor.setKeepAliveSeconds(ACTIVE_TIME);
        asyncTaskExecutor.setThreadNamePrefix("async-task-thread-pool");
        asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        asyncTaskExecutor.initialize();
        return asyncTaskExecutor;
    }
}
