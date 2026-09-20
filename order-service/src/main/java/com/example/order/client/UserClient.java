package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务 Feign 客户端，实例列表来自 MySQL 注册中心。
 */
@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * 按 ID 查询用户。
     */
    @GetMapping("/user/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long id);

}
