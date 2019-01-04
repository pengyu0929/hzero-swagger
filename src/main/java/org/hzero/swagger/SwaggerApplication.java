package org.hzero.swagger;

import org.hzero.autoconfigure.swagger.EnableHZeroSwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import io.choerodon.eureka.event.EurekaEventHandler;

/**
 * 注：Swagger 服务中，service_route、service，默认不与产品、环境做关联，都设置默认ID=0
 */
@EnableHZeroSwagger
@EnableEurekaClient
@SpringBootApplication
public class SwaggerApplication {

    public static void main(String[] args) {
        EurekaEventHandler.getInstance().init();
        SpringApplication.run(SwaggerApplication.class, args);
    }

}
