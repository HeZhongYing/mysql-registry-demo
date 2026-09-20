package com.example.console.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 控制台操作接口：实例下线/上线、权重调整、配置增删改。
 */
@RestController
@RequestMapping("/api")
public class RegistryOperationController {

    private final JdbcTemplate jdbc;

    /**
     * 注入 JdbcTemplate，直接操作注册中心表。
     */
    public RegistryOperationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 修改实例状态：UP 上线 / OFFLINE 手动下线（心跳不会自动复活）。
     */
    @PutMapping("/instances/{instanceId}/status")
    public Map<String, Object> updateStatus(@PathVariable String instanceId, @RequestBody StatusRequest request) {
        if (!"UP".equals(request.status()) && !"OFFLINE".equals(request.status())) {
            throw new IllegalArgumentException("状态仅支持 UP / OFFLINE");
        }
        int rows = jdbc.update("UPDATE service_instance SET status = ? WHERE instance_id = ?", request.status(), instanceId);
        return Map.of("updated", rows);
    }

    /**
     * 修改实例权重，多实例时影响流量分配比例。
     */
    @PutMapping("/instances/{instanceId}/weight")
    public Map<String, Object> updateWeight(@PathVariable String instanceId, @RequestBody WeightRequest request) {
        int rows = jdbc.update("UPDATE service_instance SET weight = ? WHERE instance_id = ?", request.weight(), instanceId);
        return Map.of("updated", rows);
    }

    /**
     * 新增配置。
     */
    @PostMapping("/configs")
    public Map<String, Object> createConfig(@RequestBody ConfigRequest request) {
        jdbc.update("INSERT INTO service_config (service_name, config_key, config_value) VALUES (?, ?, ?)",
                request.serviceName(), request.configKey(), request.configValue());
        return Map.of("created", 1);
    }

    /**
     * 修改配置：版本号 +1 并写入变更历史，订阅服务约 5s 后自动刷新。
     */
    @PutMapping("/configs")
    public Map<String, Object> updateConfig(@RequestBody ConfigRequest request) {
        String oldValue = jdbc.query("SELECT config_value FROM service_config WHERE service_name = ? AND config_key = ?",
                rs -> rs.next() ? rs.getString(1) : null, request.serviceName(), request.configKey());
        int rows = jdbc.update("""
                UPDATE service_config SET config_value = ?, version = version + 1
                WHERE service_name = ? AND config_key = ?
                """, request.configValue(), request.serviceName(), request.configKey());
        if (rows > 0) {
            Long version = jdbc.queryForObject(
                    "SELECT version FROM service_config WHERE service_name = ? AND config_key = ?",
                    Long.class, request.serviceName(), request.configKey());
            jdbc.update("INSERT INTO config_history (service_name, config_key, old_value, new_value, version) VALUES (?, ?, ?, ?, ?)",
                    request.serviceName(), request.configKey(), oldValue, request.configValue(), version);
        }
        return Map.of("updated", rows);
    }

    /**
     * 删除配置。
     */
    @DeleteMapping("/configs/{serviceName}/{configKey}")
    public Map<String, Object> deleteConfig(@PathVariable String serviceName, @PathVariable String configKey) {
        int rows = jdbc.update("DELETE FROM service_config WHERE service_name = ? AND config_key = ?", serviceName, configKey);
        return Map.of("deleted", rows);
    }

    /**
     * 状态修改请求体。
     */
    public record StatusRequest(String status) {
    }

    /**
     * 权重修改请求体。
     */
    public record WeightRequest(Integer weight) {
    }

    /**
     * 配置增删改请求体。
     */
    public record ConfigRequest(String serviceName, String configKey, String configValue) {
    }

}
