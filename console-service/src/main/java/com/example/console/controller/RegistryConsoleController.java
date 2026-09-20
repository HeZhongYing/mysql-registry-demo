package com.example.console.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制台查询接口：实例注册情况与配置情况。
 */
@RestController
@RequestMapping("/api")
public class RegistryConsoleController {

    private final JdbcTemplate jdbc;

    /**
     * 注入 JdbcTemplate，直接查询注册中心表。
     */
    public RegistryConsoleController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 全部服务实例的注册情况。
     */
    @GetMapping("/instances")
    public List<Map<String, Object>> listInstances() {
        return jdbc.queryForList("""
                SELECT service_name, instance_id, host, port, weight, status, last_heartbeat, registered_at
                FROM service_instance
                ORDER BY service_name, instance_id
                """);
    }

    /**
     * 全部配置文件。
     */
    @GetMapping("/configs")
    public List<Map<String, Object>> listConfigs() {
        return jdbc.queryForList("""
                SELECT service_name, file_name, format, content, version, updated_at
                FROM service_config_file
                ORDER BY service_name, file_name
                """);
    }

    /**
     * 控制台概览：实例总数、存活数、下线数、配置数、服务数。
     */
    @GetMapping("/overview")
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serverTime", LocalDateTime.now().toString());
        result.put("serviceCount", jdbc.queryForObject(
                "SELECT COUNT(DISTINCT service_name) FROM service_instance WHERE status = 'UP'", Integer.class));
        result.put("instanceUp", jdbc.queryForObject(
                "SELECT COUNT(*) FROM service_instance WHERE status = 'UP'", Integer.class));
        result.put("instanceOffline", jdbc.queryForObject(
                "SELECT COUNT(*) FROM service_instance WHERE status = 'OFFLINE'", Integer.class));
        result.put("instanceDown", jdbc.queryForObject(
                "SELECT COUNT(*) FROM service_instance WHERE status = 'DOWN'", Integer.class));
        result.put("configCount", jdbc.queryForObject(
                "SELECT COUNT(*) FROM service_config_file", Integer.class));
        return result;
    }

}
