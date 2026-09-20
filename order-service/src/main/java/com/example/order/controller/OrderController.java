package com.example.order.controller;

import com.example.order.client.ProductClient;
import com.example.order.client.UserClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final UserClient userClient;
    private final ProductClient productClient;

    public OrderController(UserClient userClient, ProductClient productClient) {
        this.userClient = userClient;
        this.productClient = productClient;
    }

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
