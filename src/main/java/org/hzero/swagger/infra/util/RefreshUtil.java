package org.hzero.swagger.infra.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hzero.swagger.config.SwaggerProperties;
import org.hzero.swagger.infra.feign.ConfigServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配置刷新操作
 *
 * @author wuguokai
 */
@Component
public class RefreshUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshUtil.class);

    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    private ConfigServerClient configServerClient;
    @Autowired
    private SwaggerProperties properties;

    /**
     * 通知config-server刷新配置
     */
    public void refresh() {
        for (String gatewayName : properties.getGatewayNames()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("path", gatewayName);
            LOGGER.info("{} :配置刷新通知", gatewayName);
            asyncExecutor.submit(() ->
                    configServerClient.refresh(map)
            );
        }
    }
}
