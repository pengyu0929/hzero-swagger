package org.hzero.swagger.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.swagger.app.ServiceRouteService;
import org.hzero.swagger.domain.entity.ServiceRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 路由Controller
 *
 * @author bojiangzhou 2018/12/14
 */
@ApiIgnore
@RestController
@RequestMapping(value = "/v1/routes")
public class RouteController extends BaseController {

    @Autowired
    private ServiceRouteService serviceRouteService;

    /**
     * 增加一个新路由
     *
     * @param route 路由信息对象
     * @return RouteDO
     */
    @Permission(level = ResourceLevel.SITE)
    @ApiOperation("增加一个新路由，必传：serviceCode/name/path")
    @PostMapping
    @ApiIgnore
    public ResponseEntity<ServiceRoute> create(@RequestBody ServiceRoute route) {
        validObject(route);
        return Results.success(serviceRouteService.create(route));
    }

}
