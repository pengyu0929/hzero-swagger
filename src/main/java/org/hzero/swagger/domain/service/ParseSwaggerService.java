package org.hzero.swagger.domain.service;

import io.choerodon.eureka.event.EurekaEventPayload;

/**
 * 解析路由
 *
 * @author bojiangzhou 2019/01/04
 */
public interface ParseSwaggerService {

    /**
     * 解析swagger的文档树
     *
     * @param payload 接受的消息
     */
    void parser(EurekaEventPayload payload);

}
