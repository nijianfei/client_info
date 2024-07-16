package com.clientInfo.csc.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // 设置连接超时和读取超时
        return builder
                .setConnectTimeout(Duration.ofMillis(3000)) // 连接超时，单位毫秒
                .setReadTimeout(Duration.ofMillis(3000)) // 读取超时，单位毫秒
                .build();
    }
}