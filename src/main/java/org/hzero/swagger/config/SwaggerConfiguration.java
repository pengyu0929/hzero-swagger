package org.hzero.swagger.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 *
 * @author bojiangzhou 2018/12/13
 */
@Configuration
@EnableConfigurationProperties
public class SwaggerConfiguration {

    @Bean
    SwaggerProperties swaggerProperties() {
        return new SwaggerProperties();
    }

}
