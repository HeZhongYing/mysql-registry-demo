package com.example.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 注册中心控制台入口，自身也注册到 MySQL 注册中心。
 */
@SpringBootApplication
public class ConsoleApplication {

    /**
     * 程序入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

}
