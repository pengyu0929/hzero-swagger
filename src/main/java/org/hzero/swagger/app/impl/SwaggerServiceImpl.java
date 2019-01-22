package org.hzero.swagger.app.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.hzero.swagger.app.SwaggerService;
import org.hzero.swagger.config.SwaggerProperties;
import org.hzero.swagger.domain.entity.ServiceRoute;
import org.hzero.swagger.domain.entity.Swagger;
import org.hzero.swagger.domain.repository.ServiceRouteRepository;
import org.hzero.swagger.domain.repository.SwaggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.eureka.event.EurekaEventPayload;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.UiConfiguration;

@Component
public class SwaggerServiceImpl implements SwaggerService {

    @Autowired
    private SwaggerProperties swaggerProperties;
    @Autowired
    private ServiceRouteRepository serviceRouteRepository;
    @Autowired
    private SwaggerRepository swaggerRepository;

    @Override
    public List<SwaggerResource> getSwaggerResource() {
        List<SwaggerResource> resources = new LinkedList<>();
        MultiKeyMap multiKeyMap = serviceRouteRepository.getAllRunningInstances();
        Set set = multiKeyMap.keySet();
        for (Object key : set) {
            MultiKey multiKey = (MultiKey) key;
            ServiceRoute route = (ServiceRoute) multiKeyMap.get(multiKey);
            if (route.getServiceCode() != null) {
                boolean isSkipService = Arrays.stream(swaggerProperties.getSkipService()).anyMatch(t -> t.equals(route.getServiceCode()));
                if (!isSkipService) {
                    SwaggerResource resource = new SwaggerResource();
                    resource.setName(route.getName() + ":" + route.getServiceCode());
                    resource.setSwaggerVersion("2.0");
                    resource.setLocation("/docs/" + route.getName() + "?version=" + multiKey.getKey(1));
                    resources.add(resource);
                }
            }
        }
        resources = resources.stream().sorted(Comparator.comparing(SwaggerResource::getName)).collect(Collectors.toList());
        return resources;
    }

    @Override
    public UiConfiguration getUiConfiguration() {
        return new UiConfiguration(null);
    }

    @Override
    public SecurityConfiguration getSecurityConfiguration() {
        return new SecurityConfiguration(
                swaggerProperties.getClient(), "unknown", "default",
                "default", "token",
                ApiKeyVehicle.HEADER, "token", ",");
    }

    @Override
    public void updateOrInsertSwagger(EurekaEventPayload payload, String json) {
        Swagger param = new Swagger();
        param.setServiceVersion(payload.getVersion());
        param.setServiceName(payload.getAppName());
        if (StringUtils.isBlank(json)) {
            throw new CommonException("无法获取Swagger文档信息，请确认能访问到您的服务！");
        }
        Swagger swagger = swaggerRepository.selectOne(param);
        if (swagger != null) {
            swagger.setValue(json);
            if (swaggerRepository.updateByPrimaryKey(swagger) != 1) {
                throw new CommonException("error.swagger.update");
            }
        } else {
            Swagger inert = new Swagger();
            inert.setServiceName(payload.getAppName());
            inert.setServiceVersion(payload.getVersion());
            inert.setValue(json);

            if (swaggerRepository.insert(inert) != 1) {
                throw new CommonException("error.swagger.insert");
            }
        }

    }

}
