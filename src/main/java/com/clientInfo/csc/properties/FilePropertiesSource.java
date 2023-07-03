package com.clientInfo.csc.properties;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 自定义的配置数据源，继承自Spring框架的 MapPropertySource 类，从一个名为 my.properties 的文件中读取配置信息，并在每10秒钟刷新一次。
 * @author Administrator
 */
@Slf4j
//@Component
public class FilePropertiesSource extends MapPropertySource {
    private static final Logger logger = LoggerFactory.getLogger(FilePropertiesSource.class);
    private static final String CONFIG_FILE_NAME = "ac.properties";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public FilePropertiesSource() {
        super("filePropertiesSource", new HashMap<>());
    }
    /**
     * 从配置文件中读取配置，10s 更新一次
     */
    @PostConstruct
    @Scheduled(fixedRate = 10_000)
    public void refreshSource() throws IOException {
        logger.info("开始读取配置文件 {}", CONFIG_FILE_NAME);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
        if (inputStream == null) {
            throw new FileNotFoundException("配置文件 " + CONFIG_FILE_NAME + " 不存在");
        }
        Map<String, String> newProperties = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.isEmpty(line) || line.startsWith("#")) {
                    continue;
                }
                String[] pair = StringUtils.split(line, "=");
                if (pair == null || pair.length != 2) {
                    logger.warn("忽略配置项 {}", line);
                    continue;
                }
                String key = pair[0].trim();
                String value = pair[1].trim();
                logger.debug("读取配置项 {} = {}", key, value);
                newProperties.put(key, value);
            }
        } catch (IOException e) {
            logger.error("读取配置文件 {} 出现异常：{}", CONFIG_FILE_NAME, e.getMessage(), e);
            throw e;
        }
        synchronized (this) {
            source.clear();
            source.putAll(newProperties);
        }
        logger.info("读取配置文件完成，共读取 {} 个配置项，时间 {}", newProperties.size(), LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }
    /**
     * 覆盖 getProperty 方法，实现实时获取配置
     *
     * @param key 配置项的 key
     * @return 配置项的值
     */
    @Override
    public Object getProperty(String key) {
        return source.get(key);
    }
}
