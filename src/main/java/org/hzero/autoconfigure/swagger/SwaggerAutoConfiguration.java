package org.hzero.autoconfigure.swagger;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IAM 自动化配置
 *
 * @author bojiangzhou 2018/10/25
 */
@ComponentScan(value = {
    "org.hzero.swagger.api",
    "org.hzero.swagger.app",
    "org.hzero.swagger.config",
    "org.hzero.swagger.domain",
    "org.hzero.swagger.infra",
})
@EnableFeignClients({"org.hzero.swagger"})
@EnableScheduling
@Configuration
public class SwaggerAutoConfiguration {

}
