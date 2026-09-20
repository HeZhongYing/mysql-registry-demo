package com.example.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品服务入口，启动后自动注册到 MySQL 注册中心。
 */
@SpringBootApplication
public class ProductServiceApplication {

    /**
     * 程序入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
