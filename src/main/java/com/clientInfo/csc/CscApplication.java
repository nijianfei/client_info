package com.clientInfo.csc;

import cn.hutool.core.io.FileUtil;
import com.clientInfo.csc.utils.SurveillanceItem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

//@EnableCaching
@EnableScheduling
@SpringBootApplication
public class CscApplication {

    public static void main(String[] args) {
        SpringApplication.run(CscApplication.class, args);
        //临时测试用
        List<String> ipList = FileUtil.readLines(new File(new File(System.getProperty("user.dir")), "ips.txt"), "utf8");
        ipList.forEach(ip-> SurveillanceItem.pingSync(ip));
    }

    @Bean
    public RestTemplate restTemplate() {
        StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();
        return restTemplate;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }
}
