package com.example.registry.loadbalancer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;

/**
 * 把加权负载均衡器注册为 LoadBalancer 全局默认配置。
 */
@AutoConfiguration
@ConditionalOnClass(ReactorServiceInstanceLoadBalancer.class)
@LoadBalancerClients(defaultConfiguration = MysqlLoadBalancerClientConfiguration.class)
public class MysqlLoadBalancerAutoConfiguration {

}
