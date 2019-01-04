package org.hzero.swagger.api.eventhandler;

import org.hzero.swagger.domain.service.ParseSwaggerService;
import org.springframework.stereotype.Component;

import io.choerodon.eureka.event.AbstractEurekaEventObserver;
import io.choerodon.eureka.event.EurekaEventPayload;

/**
 * 服务注册，解析Swagger信息
 *
 * @author bojiangzhou
 */
@Component
public class ParseSwaggerListener extends AbstractEurekaEventObserver {

    private ParseSwaggerService parseSwaggerService;

    public ParseSwaggerListener(ParseSwaggerService parseSwaggerService) {
        this.parseSwaggerService = parseSwaggerService;
    }

    @Override
    public void receiveUpEvent(EurekaEventPayload payload) {
        parseSwaggerService.parser(payload);
    }

    @Override
    public void receiveDownEvent(EurekaEventPayload payload) {
        // do nothing
    }
}
