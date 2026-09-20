package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品服务 Feign 客户端，实例列表来自 MySQL 注册中心。
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    /**
     * 按 ID 查询商品。
     */
    @GetMapping("/product/{id}")
    Map<String, Object> getProduct(@PathVariable("id") Long id);

}
