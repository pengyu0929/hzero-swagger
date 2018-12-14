package org.hzero.swagger.api.eventhandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hzero.swagger.api.dto.RegisterInstancePayload;
import org.hzero.swagger.app.DocumentService;
import org.hzero.swagger.app.ServiceRouteService;
import org.hzero.swagger.app.SwaggerService;
import org.hzero.swagger.config.SwaggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * eureka-instance消息队列的新消息监听处理
 *
 * @author zhipeng.zuo
 * @author wuguokai
 */
@Component
@RefreshScope
public class EurekaInstanceRegisteredListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurekaInstanceRegisteredListener.class);

    private static final String STATUS_UP = "UP";

    private static final String REGISTER_TOPIC = "register-server";

    private final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, Integer> FAIL_TIME_MAP = new HashMap<>();

    @Autowired
    private SwaggerProperties properties;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private SwaggerService swaggerService;
    @Autowired
    private ServiceRouteService routeService;
    @Autowired
    private KafkaTemplate<byte[], byte[]> kafkaTemplate;


    /**
     * 监听eureka-instance消息队列的新消息处理
     *
     * @param record 消息信息
     */
    @KafkaListener(topics = REGISTER_TOPIC)
    public void handle(ConsumerRecord<byte[], byte[]> record) {
        String message = new String(record.value());
        try {
            LOGGER.info("receive message from register-server, {}", message);
            RegisterInstancePayload payload = mapper.readValue(message, RegisterInstancePayload.class);
            if (!STATUS_UP.equals(payload.getStatus())) {
                LOGGER.info("skip message that status is not up, {}", payload);
                return;
            }
            boolean isSkipService =
                    Arrays.stream(properties.getSkipService()).anyMatch(t -> t.equals(payload.getAppName()));
            if (isSkipService) {
                LOGGER.info("skip message that is skipServices, {}", payload);
                return;
            }
            Observable.just(payload)
                    .delay(properties.getFetchSeconds(), TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::msgConsumer);
        } catch (IOException e) {
            LOGGER.warn("error happened when handle message， {} cause {}", message, e.getCause());
        }
    }

    private void msgConsumer(final RegisterInstancePayload instancePayload) {
        String json = documentService.fetchSwaggerJsonByIp(instancePayload);
        if (StringUtils.isEmpty(json)) {
            LOGGER.info("fetched swagger json data is empty, {}", instancePayload);
            Integer time = FAIL_TIME_MAP.get(instancePayload.getInstanceAddress());
            if (time == null) {
                time = 0;
            }
            if (properties.isFetchCallback()) {
                instancePayload.setApiData(null);
                try {
                    if (time < properties.getFetchTime()) {
                        kafkaTemplate.send(REGISTER_TOPIC, mapper.writeValueAsBytes(instancePayload));
                        FAIL_TIME_MAP.put(instancePayload.getInstanceAddress(), ++time);
                    } else {
                        FAIL_TIME_MAP.remove(instancePayload.getInstanceAddress());
                        LOGGER.warn("fetched swagger json data failed too many times {}", instancePayload);
                    }

                } catch (JsonProcessingException e) {
                    LOGGER.warn("error happened when instancePayload serialize {}", e.getMessage());
                }
            }
        } else {
            swaggerConsumer(instancePayload, json);
            routeConsumer(json);
            FAIL_TIME_MAP.remove(instancePayload.getInstanceAddress());
        }
    }

    private void swaggerConsumer(final RegisterInstancePayload payload, final String json) {
        try {
            swaggerService.updateOrInsertSwagger(payload, json);
        } catch (Exception e) {
            LOGGER.warn("message has bean consumed failed when updateOrInsertSwagger, e {}", e.getMessage());
        }
    }

    private void routeConsumer(final String json) {
        try {
            routeService.refreshRoute(json);
        } catch (Exception e) {
            LOGGER.warn("message has bean consumed failed when refreshRoute, e {}", e.getMessage());
        }
    }
}

