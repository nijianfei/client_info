package com.clientInfo.csc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolConfig implements AsyncConfigurer {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20); // 根据需求调整
        scheduler.setThreadNamePrefix("scheduled-task-");
        return scheduler;
    }
}