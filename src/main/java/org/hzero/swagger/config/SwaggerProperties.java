package org.hzero.swagger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * gateway类型的服务名称列表
 */
@ConfigurationProperties(prefix = SwaggerProperties.PREFIX)
public class SwaggerProperties {

    static final String PREFIX = "hzero.swagger";

    /**
     * OAuth认证地址
     */
    private String oauthUrl = "http://localhost:8080/oauth/oauth/authorize}";
    /**
     * Client
     */
    private String client = "client";
    /**
     * 网关地址
     */
    private String gatewayDomain = "localhost";
    /**
     * 网关名称
     */
    private String[] gatewayNames = new String[]{"api-gateway", "gateway-helper"};
    /**
     * 跳过的服务
     */
    private String[] skipService = new String[]{};
    /**
     * 拉取Swagger的次数
     */
    private int fetchTime = 10;
    /**
     * 拉取Swagger间隔秒数
     */
    private int fetchSeconds = 60;
    /**
     * 拉取Swagger是否需要重试
     */
    private boolean fetchCallback = true;

    public String getOauthUrl() {
        return oauthUrl;
    }

    public void setOauthUrl(String oauthUrl) {
        this.oauthUrl = oauthUrl;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getGatewayDomain() {
        return gatewayDomain;
    }

    public void setGatewayDomain(String gatewayDomain) {
        this.gatewayDomain = gatewayDomain;
    }

    public String[] getGatewayNames() {
        return gatewayNames;
    }

    public void setGatewayNames(String[] gatewayNames) {
        this.gatewayNames = gatewayNames;
    }

    public String[] getSkipService() {
        return skipService;
    }

    public void setSkipService(String[] skipService) {
        this.skipService = skipService;
    }

    public int getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(int fetchTime) {
        this.fetchTime = fetchTime;
    }

    public int getFetchSeconds() {
        return fetchSeconds;
    }

    public void setFetchSeconds(int fetchSeconds) {
        this.fetchSeconds = fetchSeconds;
    }

    public boolean isFetchCallback() {
        return fetchCallback;
    }

    public void setFetchCallback(boolean fetchCallback) {
        this.fetchCallback = fetchCallback;
    }
}
