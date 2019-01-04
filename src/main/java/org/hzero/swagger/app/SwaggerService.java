package org.hzero.swagger.app;

import java.util.List;

import io.choerodon.eureka.event.EurekaEventPayload;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.UiConfiguration;

public interface SwaggerService {

    List<SwaggerResource> getSwaggerResource();

    UiConfiguration getUiConfiguration();

    SecurityConfiguration getSecurityConfiguration();

    /**
     * 更新或者插入swagger json
     *
     * @param payload 存储service信息的实体
     * @param json                    swagger json
     */
    void updateOrInsertSwagger(EurekaEventPayload payload, String json);

}
