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

    @Override
    public String getInstanceId() {
        return host + ":" + port;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return URI.create("http://" + host + ":" + port);
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

}
