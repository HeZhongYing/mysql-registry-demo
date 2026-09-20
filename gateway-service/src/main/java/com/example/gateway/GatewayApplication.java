package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务入口，路由目标通过 MySQL 注册中心按服务名解析。
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * 程序入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
