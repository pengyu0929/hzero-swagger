package org.hzero.swagger.domain.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.eureka.event.EurekaEventPayload;
import org.apache.commons.lang3.StringUtils;
import org.hzero.swagger.app.SwaggerService;
import org.hzero.swagger.domain.service.ParseSwaggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author bojiangzhou 2019/01/04
 */
@Component
public class ParseSwaggerServiceImpl implements ParseSwaggerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseSwaggerServiceImpl.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private SwaggerService swaggerService;

    @Override
    public void parser(EurekaEventPayload payload) {
        try {
            fetchSwaggerJsonByIp(payload);
            String serviceName = payload.getAppName();
            String json = payload.getApiData();
            LOGGER.info("receive service: {} message, version: {}, ip: {}", serviceName, payload.getVersion(), payload.getInstanceAddress());

            if (!StringUtils.isEmpty(serviceName) && !StringUtils.isEmpty(json)) {
                try {
                    swaggerService.updateOrInsertSwagger(payload, json);
                } catch (Exception e) {
                    LOGGER.warn("message has bean consumed failed when updateOrInsertSwagger, e {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new CommonException("refresh service route error. [serviceName={}]", payload.getId());
        }
    }

    private void fetchSwaggerJsonByIp(final EurekaEventPayload payload) {
        ResponseEntity<String> response = restTemplate.getForEntity("http://" + payload.getInstanceAddress() + "/v2/choerodon/api-docs",
                String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            payload.setApiData(response.getBody());
        } else {
            LOGGER.warn("fetch swagger error, statusCode is not 2XX, serviceId: {}", payload.getId());
            throw new CommonException("fetch swagger error.");
        }
    }

}
