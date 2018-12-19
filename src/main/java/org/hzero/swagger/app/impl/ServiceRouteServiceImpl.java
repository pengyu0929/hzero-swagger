package org.hzero.swagger.app.impl;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.swagger.ChoerodonRouteData;
import io.choerodon.swagger.swagger.extra.ExtraData;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.swagger.app.ServiceRouteService;
import org.hzero.swagger.domain.entity.HService;
import org.hzero.swagger.domain.entity.ServiceRoute;
import org.hzero.swagger.domain.repository.HServiceRepository;
import org.hzero.swagger.domain.repository.ServiceRouteRepository;
import org.hzero.swagger.infra.constant.Governance;
import org.hzero.swagger.infra.util.RefreshUtil;
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
    @Autowired
    private RefreshUtil refreshUtil;

    @Override
    public void refreshRoute(String serviceName, String swaggerJson) {
        ChoerodonRouteData data = extractRouteData(swaggerJson);
        if (data == null) {
            throw new CommonException("refresh route error, cant't parse route data. check if config ExtraDataManager.");
        }

        // 服务 ExtraData 返回的是标准的服务名，开发环境中带工号的需自动处理下
        String serviceId = data.getServiceId();
        if (!StringUtils.equals(serviceId, serviceName) && serviceName.startsWith(serviceId)) {
            String suffix = serviceName.replace(serviceId, ""); // 截取后缀
            data.setServiceId(serviceName);
            data.setName(data.getName() + suffix);
            data.setPath(data.getPath().replace("/**", "") + suffix + "/**");
        }

        executeRefreshRoute(data);
    }

    private ChoerodonRouteData extractRouteData(String swaggerJson) {
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
                        return data;
                    }
                }
            }
        }
        return null;
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
        refreshUtil.refresh();
    }

    private void setRoute(ChoerodonRouteData routeData, ServiceRoute route) {
        route.setName(routeData.getName());
        route.setPath(routeData.getPath());
        route.setServiceCode(routeData.getServiceId());
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
            route.setServiceCode(route.getServiceCode());
        } else {
            HService insert = new HService();
            insert.setServiceCode(route.getServiceCode());
            insert.setServiceName(route.getServiceCode());
            insert.setAppSourceId(Governance.DEFAULT_ID);
            serviceRepository.insertSelective(insert);
            route.setServiceId(insert.getServiceId());
            route.setServiceCode(route.getServiceCode());
        }
    }

}
