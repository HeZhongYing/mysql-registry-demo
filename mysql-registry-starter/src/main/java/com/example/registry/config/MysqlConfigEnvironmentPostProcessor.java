package com.example.registry.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动期从 service_config_file 表加载配置文件，解析合并后以最高优先级注入 Environment。
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
        Map<String, Object> configs = loadAndParse(url, username, password,
                environment.getProperty("spring.application.name", "application"));
        if (!configs.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, configs));
        }
    }

    /**
     * 加载全局与服务级配置文件并解析合并，服务级覆盖全局。
     */
    public static Map<String, Object> loadAndParse(String url, String username, String password, String serviceName) {
        List<String[]> files = new ArrayList<>();
        String sql = "SELECT file_name, format, content FROM service_config_file " +
                "WHERE service_name IN ('application', '" + serviceName.replace("'", "''") + "') " +
                "ORDER BY CASE service_name WHEN 'application' THEN 0 ELSE 1 END, file_name";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                files.add(new String[]{rs.getString("file_name"), rs.getString("format"), rs.getString("content")});
            }
        } catch (Exception e) {
            throw new IllegalStateException("加载 MySQL 配置文件失败", e);
        }
        Map<String, Object> configs = new LinkedHashMap<>();
        for (String[] file : files) {
            configs.putAll(ConfigFileParser.parse(file[0], file[1], file[2]));
        }
        return configs;
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

}
