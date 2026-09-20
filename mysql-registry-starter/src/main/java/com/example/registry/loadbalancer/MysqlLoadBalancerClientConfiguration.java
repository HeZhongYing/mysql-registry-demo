package com.example.registry.loadbalancer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LoadBalancer 子上下文的默认配置：注册加权负载均衡器，替换默认轮询。
 * Feign（阻塞式）与 Gateway（响应式）共用此配置。
 */
@Configuration(proxyBeanMethods = false)
public class MysqlLoadBalancerClientConfiguration {

    /**
     * 加权随机负载均衡器 Bean。
     */
    @Bean
    public ReactorServiceInstanceLoadBalancer weightedServiceInstanceLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> supplierProvider) {
        return new WeightedServiceInstanceLoadBalancer(supplierProvider);
    }

}
