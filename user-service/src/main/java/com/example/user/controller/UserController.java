package com.example.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户接口。@RefreshScope 使 greeting 配置变更后无需重启即可生效。
 */
@RefreshScope
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 问候语，取自 MySQL 配置中心的 user.greeting。
     */
    @Value("${user.greeting:hello-from-default}")
    private String greeting;

    /**
     * 按 ID 查询用户。
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        return Map.of(
                "id", id,
                "name", "用户" + id,
                "greeting", greeting
        );
    }

}
