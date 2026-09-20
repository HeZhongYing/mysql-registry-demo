package com.example.registry.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 启动期从 service_config 表加载配置，优先级最高（覆盖本地 yml）。
 * 全局配置（application）先加载，服务级配置后加载并覆盖全局。
 */
public class MysqlConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE_NAME = "mysqlRegistry";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!"true".equals(environment.getProperty("mysql-registry.enabled", "true"))) {
            return;
        }
        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        if (url == null) {
            return;
        }
        Map<String, Object> configs = loadConfigs(url, username, password,
                environment.getProperty("spring.application.name", "application"));
        if (!configs.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, configs));
        }
    }

    /**
     * 查询全局配置与服务级配置，服务级覆盖全局。
     */
    public static Map<String, Object> loadConfigs(String url, String username, String password, String serviceName) {
        Map<String, Object> configs = new LinkedHashMap<>();
        String sql = "SELECT service_name, config_key, config_value FROM service_config " +
                "WHERE service_name IN ('application', '" + serviceName.replace("'", "''") + "') " +
                "ORDER BY CASE service_name WHEN 'application' THEN 0 ELSE 1 END";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                configs.put(rs.getString("config_key"), rs.getString("config_value"));
            }
        } catch (Exception e) {
            throw new IllegalStateException("加载 MySQL 配置失败", e);
        }
        return configs;
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

}
