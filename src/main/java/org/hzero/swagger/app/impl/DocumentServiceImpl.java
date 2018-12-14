package org.hzero.swagger.app.impl;

import java.io.IOException;
import java.util.*;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.hzero.swagger.api.dto.RegisterInstancePayload;
import org.hzero.swagger.app.DocumentService;
import org.hzero.swagger.app.ServiceRouteService;
import org.hzero.swagger.app.SwaggerService;
import org.hzero.swagger.config.SwaggerProperties;
import org.hzero.swagger.domain.entity.ServiceRoute;
import org.hzero.swagger.domain.entity.Swagger;
import org.hzero.swagger.domain.repository.ServiceRouteRepository;
import org.hzero.swagger.domain.repository.SwaggerRepository;
import org.hzero.swagger.infra.constant.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.models.auth.OAuth2Definition;

/**
 * 实现类
 */
@Component
public class DocumentServiceImpl implements DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private static final String METADATA_CONTEXT = "CONTEXT";
    private static final String DEFAULT = "default";


    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private SwaggerProperties swaggerProperties;
    @Autowired
    private SwaggerService swaggerService;
    @Autowired
    private SwaggerRepository swaggerRepository;
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private ServiceRouteRepository serviceRouteRepository;
    @Autowired
    private ServiceRouteService routeService;

    @Override
    public String getSwaggerJson(String name, String version) throws IOException {
        MultiKeyMap multiKeyMap = serviceRouteRepository.getAllRunningInstances();
        ServiceRoute route = getRouteFromRunningInstancesMap(multiKeyMap, name, version);
        if (route == null) {
            return "";
        }
        String basePath = route.getPath().replace("/**", "");

        ObjectNode root = getSwaggerJsonByIdAndVersion(route.getServiceCode(), version);
        root.put("basePath", basePath);
        root.put("host", swaggerProperties.getGatewayDomain());
        LOGGER.debug("put basePath:{}, host:{}", basePath, root.get("host"));
        return MAPPER.writeValueAsString(root);
    }

    private ServiceRoute getRouteFromRunningInstancesMap(MultiKeyMap runningMap, String name, String version) {
        for (Object object : runningMap.values()) {
            if (object instanceof ServiceRoute) {
                ServiceRoute route = (ServiceRoute) object;
                if (name.equals(route.getName())) {
                    return route;
                }
            }
        }
        return null;
    }

    public ObjectNode getSwaggerJsonByIdAndVersion(String service, String version) throws IOException {
        String json = fetchSwaggerJsonByService(service, version);
        if (StringUtils.isEmpty(json)) {
            throw new RemoteAccessException("fetch swagger json failed");
        }
        ObjectNode node = (ObjectNode) MAPPER.readTree(json);
        List<Map<String, List<String>>> security = new LinkedList<>();
        Map<String, List<String>> clients = new TreeMap<>();
        clients.put(swaggerProperties.getClient(), Collections.singletonList(DEFAULT));
        security.add(clients);
        OAuth2Definition definition = new OAuth2Definition();
        definition.setAuthorizationUrl(swaggerProperties.getOauthUrl());
        definition.setType("oauth2");
        definition.setFlow("implicit");
        definition.setScopes(Collections.singletonMap(DEFAULT, "default scope"));
        LOGGER.info("{}", definition.getScopes());
        node.putPOJO("securityDefinitions", Collections.singletonMap(swaggerProperties.getClient(), definition));
        Iterator<Map.Entry<String, JsonNode>> pathIterator = node.get("paths").fields();
        while (pathIterator.hasNext()) {
            Map.Entry<String, JsonNode> pathNode = pathIterator.next();
            Iterator<Map.Entry<String, JsonNode>> methodIterator = pathNode.getValue().fields();
            while (methodIterator.hasNext()) {
                Map.Entry<String, JsonNode> methodNode = methodIterator.next();
                ((ObjectNode) methodNode.getValue()).putPOJO("security", security);
            }
        }
        return node;
    }

    public String fetchSwaggerJsonByService(String service, String version) {
        Swagger param = new Swagger();
        param.setServiceName(service);
        param.setServiceVersion(version);
        Swagger swagger = swaggerRepository.selectOne(param);
        if (swagger == null || StringUtils.isEmpty(swagger.getValue())) {
            return getJsonByNameAndVersion(service, version);
        } else {
            return swagger.getValue();
        }
    }

    private String getJsonByNameAndVersion(String service, String version) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);
        for (ServiceInstance instance : instances) {
            String mdVersion = instance.getMetadata().get(Versions.METADATA_VERSION);
            if (StringUtils.isEmpty(mdVersion)) {
                mdVersion = Versions.NULL_VERSION;
            }
            if (version.equals(mdVersion)) {
                return fetch(instance);
            }
        }
        return null;
    }

    @Override
    public void manualRefresh(String serviceName, String version) {
        String json = fetchSwaggerJsonByService(serviceName, version);
        RegisterInstancePayload registerInstancePayload = new RegisterInstancePayload();
        registerInstancePayload.setAppName(serviceName);
        registerInstancePayload.setVersion(version);
        swaggerService.updateOrInsertSwagger(registerInstancePayload, json);
        routeService.refreshRoute(serviceName, json);
    }

    private String fetch(ServiceInstance instance) {
        ResponseEntity<String> response;
        String contextPath = instance.getMetadata().get(METADATA_CONTEXT);
        if (contextPath == null) {
            contextPath = "";
        }
        LOGGER.info("service: {} metadata : {}", instance.getServiceId(), instance.getMetadata());
        try {
            response = restTemplate.getForEntity(
                    instance.getUri() + contextPath + "/v2/choerodon/api-docs",
                    String.class);
        } catch (RestClientException e) {
            throw new RemoteAccessException("fetch failed, instance:" + instance.getServiceId());
        }
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RemoteAccessException("fetch failed : " + response);
        }
        return response.getBody();
    }

    @Override
    public String fetchSwaggerJsonByIp(final RegisterInstancePayload payload) {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(payload.getAppName());
        for (ServiceInstance serviceInstance : serviceInstances) {
            String instanceAddress = serviceInstance.getHost() + ":" + serviceInstance.getPort();
            if (instanceAddress.equals(payload.getInstanceAddress())) {
                return fetchByIp(payload, serviceInstance);
            }
        }
        return null;
    }

    private String fetchByIp(final RegisterInstancePayload payload, ServiceInstance instance) {
        ResponseEntity<String> response;
        String contextPath = instance.getMetadata().get(METADATA_CONTEXT);
        if (contextPath == null) {
            contextPath = "";
        }
        LOGGER.info("service: {} metadata : {}" + instance.getMetadata());
        try {
            response = restTemplate.getForEntity("http://" + payload.getInstanceAddress() + contextPath + "/v2/choerodon/api-docs",
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.info("error.IDocumentService.fetchSwaggerJsonByIp {}", e.getMessage());
        }
        return null;
    }
}
