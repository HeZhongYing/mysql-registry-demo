package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务入口，启动后自动注册到 MySQL 注册中心。
 */
@SpringBootApplication
public class UserServiceApplication {

    /**
     * 程序入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
