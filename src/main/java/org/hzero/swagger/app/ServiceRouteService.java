package org.hzero.swagger.app;

import org.hzero.swagger.domain.entity.ServiceRoute;

/**
 * 路由信息操作业务service
 */
public interface ServiceRouteService {

    void refreshRoute(String swaggerJson);

    ServiceRoute create(ServiceRoute route);

    ServiceRoute update(ServiceRoute route);
}
