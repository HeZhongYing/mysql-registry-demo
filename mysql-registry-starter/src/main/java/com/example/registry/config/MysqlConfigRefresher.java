package com.example.registry.config;

import com.example.registry.MysqlRegistryProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 配置动态刷新：轮询 service_config，发现变更后更新 Environment 并发布 RefreshEvent。
 */
public class MysqlConfigRefresher {

    private static final Log log = LogFactory.getLog(MysqlConfigRefresher.class);

    private final JdbcTemplate jdbc;
    private final ConfigurableEnvironment environment;
    private final ApplicationEventPublisher publisher;

    private volatile String lastFingerprint;

    public MysqlConfigRefresher(JdbcTemplate jdbc, ConfigurableEnvironment environment, ApplicationEventPublisher publisher) {
        this.jdbc = jdbc;
        this.environment = environment;
        this.publisher = publisher;
    }

    /**
     * 轮询配置变更。
     */
    @Scheduled(fixedDelayString = "${mysql-registry.config-poll-interval:5000}")
    public void poll() {
        try {
            String serviceName = environment.getProperty("spring.application.name", "application");
            Map<String, Object> configs = loadConfigs(serviceName);
            String fingerprint = fingerprint(configs);
            if (Objects.equals(fingerprint, lastFingerprint)) {
                return;
            }
            if (lastFingerprint == null) {
                lastFingerprint = fingerprint;
                return;
            }
            lastFingerprint = fingerprint;
            updateEnvironment(configs);
            publisher.publishEvent(new RefreshEvent(this, fingerprint, "MySQL 配置变更"));
            log.info("检测到 MySQL 配置变更，已触发刷新: " + fingerprint);
        } catch (Exception e) {
            log.warn("配置轮询失败: " + e.getMessage());
        }
    }

    /**
     * 查询全局配置与服务级配置，服务级覆盖全局。
     */
    private Map<String, Object> loadConfigs(String serviceName) {
        Map<String, Object> configs = new LinkedHashMap<>();
        jdbc.query("SELECT service_name, config_key, config_value FROM service_config " +
                        "WHERE service_name IN ('application', ?) " +
                        "ORDER BY CASE service_name WHEN 'application' THEN 0 ELSE 1 END",
                rs -> {
                    configs.put(rs.getString("config_key"), rs.getString("config_value"));
                }, serviceName);
        return configs;
    }

    /**
     * 生成配置指纹，用于对比两次轮询之间配置是否变化。
     */
    private String fingerprint(Map<String, Object> configs) {
        return String.join(";", configs.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .toList());
    }

    /**
     * 替换 Environment 中的 mysqlRegistry 属性源。
     */
    /**
     * 替换 Environment 中的 mysqlRegistry 属性源，并广播变更的配置键。
     */
    private void updateEnvironment(Map<String, Object> configs) {
        MutablePropertySources sources = environment.getPropertySources();
        sources.remove(MysqlConfigEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
        sources.addFirst(new MapPropertySource(MysqlConfigEnvironmentPostProcessor.PROPERTY_SOURCE_NAME, configs));
        publisher.publishEvent(new EnvironmentChangeEvent(this, java.util.Set.copyOf(configs.keySet())));
    }

}
