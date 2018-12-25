package org.hzero.swagger.infra.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hzero.swagger.infra.feign.ConfigServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配置刷新操作
 *
 * @author bojiangzhou
 * @author wuguokai
 */
@Component
public class RefreshUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshUtil.class);

    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    private ConfigServerClient configServerClient;

    /**
     * 通知config-server刷新配置
     */
    public void refreshRoute() {
        LOGGER.debug("Notify gateway refresh route.");
        asyncExecutor.submit(() ->
            configServerClient.refreshRoute()
        );
    }
}
