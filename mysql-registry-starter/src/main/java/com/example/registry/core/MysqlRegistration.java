package com.example.registry.core;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Map;

/**
 * 基于 MySQL 注册中心的实例注册信息。
 */
public class MysqlRegistration implements Registration {

    private final String serviceId;
    private final String host;
    private final int port;
    private final Map<String, String> metadata;

    public MysqlRegistration(String serviceId, String host, int port, Map<String, String> metadata) {
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.metadata = metadata;
    }

    /**
     * 实例唯一标识，格式为 host:port。
     */
    @Override
    public String getInstanceId() {
        return host + ":" + port;
    }

    /**
     * 服务名，对应 spring.application.name。
     */
    @Override
    public String getServiceId() {
        return serviceId;
    }

    /**
     * 实例 IP。
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * 实例端口。
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * 是否 HTTPS，当前仅支持 HTTP。
     */
    @Override
    public boolean isSecure() {
        return false;
    }

    /**
     * 实例访问地址。
     */
    @Override
    public URI getUri() {
        return URI.create("http://" + host + ":" + port);
    }

    /**
     * 实例扩展元数据。
     */
    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

}
