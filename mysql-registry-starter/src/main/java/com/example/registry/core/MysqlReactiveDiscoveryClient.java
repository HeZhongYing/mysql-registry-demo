package com.example.registry.core;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;

/**
 * 响应式服务发现，包装阻塞实现，供 Gateway（WebFlux）使用。
 */
public class MysqlReactiveDiscoveryClient implements ReactiveDiscoveryClient {

    private final MysqlDiscoveryClient delegate;

    public MysqlReactiveDiscoveryClient(MysqlDiscoveryClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public String description() {
        return delegate.description();
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        return Flux.fromIterable(delegate.getInstances(serviceId));
    }

    @Override
    public Flux<String> getServices() {
        return Flux.fromIterable(delegate.getServices());
    }

}
