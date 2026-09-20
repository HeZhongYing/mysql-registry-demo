package com.example.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品接口。
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    /**
     * 按 ID 查询商品。
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        return Map.of(
                "id", id,
                "name", "商品" + id,
                "price", 99.9
        );
    }

}
