package org.hzero.swagger.app;

import org.hzero.swagger.domain.entity.ServiceRoute;

/**
 * 路由信息操作业务service
 */
public interface ServiceRouteService {

    void refreshRoute(String serviceName, String swaggerJson);

}
