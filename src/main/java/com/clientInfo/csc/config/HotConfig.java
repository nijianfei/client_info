package com.clientInfo.csc.config;

import com.clientInfo.csc.properties.FilePropertiesSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class HotConfig {
    @Bean
    public FilePropertiesSource filePropertiesSource(ConfigurableEnvironment environment) {
        FilePropertiesSource filePropertiesSource = new FilePropertiesSource();
        // 属性源是按照添加的顺序进行合并的，后添加的属性源中的属性会覆盖前面添加的属性源中的同名属性。
        // 因此，为了确保我们自定义的属性源中的属性优先级最高，我们需要将它添加到属性源列表的最后。这样就能保证后添加的属性源中的属性值会覆盖之前的同名属性。
        environment.getPropertySources().addLast(filePropertiesSource);
        return filePropertiesSource;
    }
}
