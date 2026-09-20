package com.example.order.controller;

import com.example.order.client.ProductClient;
import com.example.order.client.UserClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单接口，聚合用户与商品数据，演示基于 MySQL 服务发现的 Feign 跨服务调用。
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private final UserClient userClient;
    private final ProductClient productClient;

    /**
     * 注入 Feign 客户端。
     */
    public OrderController(UserClient userClient, ProductClient productClient) {
        this.userClient = userClient;
        this.productClient = productClient;
    }

    /**
     * 按 ID 查询订单，返回聚合后的用户与商品信息。
     */
    @GetMapping("/{id}")
    public Map<String, Object> getOrder(@PathVariable Long id) {
        Map<String, Object> user = userClient.getUser(100L);
        Map<String, Object> product = productClient.getProduct(200L);
        return Map.of(
                "orderId", id,
                "user", user,
                "product", product
        );
    }

}
