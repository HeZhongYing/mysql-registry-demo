package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务入口，启动后自动注册到 MySQL 注册中心，并开启 Feign 客户端扫描。
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    /**
     * 程序入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
