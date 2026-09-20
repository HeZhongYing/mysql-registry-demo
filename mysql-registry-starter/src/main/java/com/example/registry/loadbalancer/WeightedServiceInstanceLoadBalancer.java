package com.example.registry.loadbalancer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

/**
 * 加权随机负载均衡器：按实例 metadata 中的 weight（默认 100）分配流量。
 */
public class WeightedServiceInstanceLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Log log = LogFactory.getLog(WeightedServiceInstanceLoadBalancer.class);

    private final ObjectProvider<ServiceInstanceListSupplier> supplier;
    private final Random random = new Random();

    public WeightedServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> supplier) {
        this.supplier = supplier;
    }

    /**
     * 从实例列表提供者获取实例并加权随机选择一个。
     */
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier listSupplier = supplier.getIfAvailable();
        if (listSupplier == null) {
            return Mono.just(new EmptyResponse());
        }
        return listSupplier.get(request).next().map(this::chooseInstance);
    }

    /**
     * 加权随机选择：权重越大被选中概率越高。
     */
    private Response<ServiceInstance> chooseInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return new EmptyResponse();
        }
        int total = instances.stream().mapToInt(this::weight).sum();
        if (total <= 0) {
            return new DefaultResponse(instances.get(random.nextInt(instances.size())));
        }
        int point = random.nextInt(total);
        int accumulated = 0;
        for (ServiceInstance instance : instances) {
            accumulated += weight(instance);
            if (point < accumulated) {
                log.debug("选中实例: " + instance.getInstanceId() + " (weight=" + weight(instance) + ")");
                return new DefaultResponse(instance);
            }
        }
        return new DefaultResponse(instances.get(instances.size() - 1));
    }

    /**
     * 读取实例权重，非法值按 100 处理。
     */
    private int weight(ServiceInstance instance) {
        try {
            return Integer.parseInt(instance.getMetadata().getOrDefault("weight", "100"));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

}
