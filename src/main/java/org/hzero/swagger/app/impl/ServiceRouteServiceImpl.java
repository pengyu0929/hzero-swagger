package org.hzero.swagger.app.impl;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.swagger.ChoerodonRouteData;
import io.choerodon.swagger.swagger.extra.ExtraData;
import org.hzero.core.base.BaseConstants;
import org.hzero.swagger.app.ServiceRouteService;
import org.hzero.swagger.domain.entity.HService;
import org.hzero.swagger.domain.entity.ServiceRoute;
import org.hzero.swagger.domain.repository.HServiceRepository;
import org.hzero.swagger.domain.repository.ServiceRouteRepository;
import org.hzero.swagger.infra.constant.Governance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bojiangzhou 2018/12/13
 */
@Service
public class ServiceRouteServiceImpl implements ServiceRouteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRouteServiceImpl.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ServiceRouteRepository serviceRouteRepository;
    @Autowired
    private HServiceRepository serviceRepository;

    @Override
    public void refreshRoute(String swaggerJson) {
        ExtraData extraData;
        Map swaggerMap = null;
        ChoerodonRouteData data;
        try {
            swaggerMap = mapper.readValue(swaggerJson, Map.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        if (swaggerMap != null) {
            Object object = swaggerMap.get(ExtraData.EXTRA_DATA_KEY);
            if (object != null) {
                extraData = mapper.convertValue(object, ExtraData.class);
                if (extraData != null) {
                    data = mapper.convertValue(extraData.getData().get(ExtraData.ZUUL_ROUTE_DATA), ChoerodonRouteData.class);
                    if (data != null) {
                        executeRefreshRoute(data);
                    }
                }
            }
        }
    }

    private void executeRefreshRoute(final ChoerodonRouteData data) {
        ServiceRoute route = new ServiceRoute();
        setRoute(data, route);
        setRouteService(route);

        ServiceRoute param = new ServiceRoute();
        param.setName(route.getName());
        param.setPath(route.getPath());
        ServiceRoute self = serviceRouteRepository.selectOne(param);

        if (self == null) {
            serviceRouteRepository.insertSelective(route);
            LOGGER.info("{} : 初始化路由成功", route.getName());
        } else {
            route.setObjectVersionNumber(self.getObjectVersionNumber());
            route.setServiceRouteId(self.getServiceRouteId());
            serviceRouteRepository.updateByPrimaryKey(route);
            LOGGER.info("{} : rout update success", route.getName());
        }
    }

    private void setRoute(ChoerodonRouteData routeData, ServiceRoute route) {
        route.setName(routeData.getName());
        route.setPath(routeData.getPath());
        route.setServiceCode(routeData.getServiceId().toUpperCase());
        route.setRetryable(routeData.getRetryable() != null && routeData.getRetryable() ? BaseConstants.Flag.YES : BaseConstants.Flag.NO);
        route.setCustomSensitiveHeaders(routeData.getCustomSensitiveHeaders() != null && routeData.getCustomSensitiveHeaders() ? BaseConstants.Flag.YES : BaseConstants.Flag.NO);
        route.setHelperService(routeData.getHelperService());
        route.setSensitiveHeaders(routeData.getSensitiveHeaders());
        route.setStripPrefix(routeData.getStripPrefix() != null && routeData.getStripPrefix() ? BaseConstants.Flag.YES : BaseConstants.Flag.NO);
        route.setUrl(routeData.getUrl());
        route.defaultProduct();
    }

    private void setRouteService(ServiceRoute route) {
        HService queryService = new HService();
        queryService.setServiceCode(route.getServiceCode());
        queryService.setAppSourceId(Governance.DEFAULT_ID);
        queryService = serviceRepository.selectOne(queryService);
        if (queryService != null) {
            route.setServiceId(queryService.getServiceId());
        } else {
            HService insert = new HService();
            insert.setServiceCode(route.getServiceCode());
            insert.setServiceName(route.getServiceCode());
            insert.setAppSourceId(Governance.DEFAULT_ID);
            serviceRepository.insertSelective(insert);
            route.setServiceId(insert.getServiceId());
        }
    }

}
