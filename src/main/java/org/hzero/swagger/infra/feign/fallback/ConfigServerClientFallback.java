package org.hzero.swagger.infra.feign.fallback;

import java.util.Map;

import org.hzero.swagger.infra.feign.ConfigServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 向config-server服务发送feign失败的回调处理
 *
 * @author wuguokai
 */
@Component
public class ConfigServerClientFallback implements ConfigServerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServerClientFallback.class);

    @Override
    public ResponseEntity<String> refresh(Map<String, ?> queryMap) {
        LOGGER.error("refresh config failed! map: {}", queryMap);
        return new ResponseEntity<>("refresh config failed!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
