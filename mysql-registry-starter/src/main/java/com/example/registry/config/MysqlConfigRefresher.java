package com.example.registry.config;

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
import java.util.Set;

/**
 * 配置动态刷新：轮询 service_config_file，发现内容变更后更新 Environment 并发布 RefreshEvent。
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
     * 轮询配置文件变更。
     */
    @Scheduled(fixedDelayString = "${mysql-registry.config-poll-interval:5000}")
    public void poll() {
        try {
            String serviceName = environment.getProperty("spring.application.name", "application");
            Map<String, Object> configs = loadAndParse(serviceName);
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
            log.info("检测到 MySQL 配置文件变更，已触发刷新: " + fingerprint);
        } catch (Exception e) {
            log.warn("配置轮询失败: " + e.getMessage());
        }
    }

    /**
     * 查询全局与服务级配置文件并解析合并，服务级覆盖全局。
     */
    private Map<String, Object> loadAndParse(String serviceName) {
        Map<String, Object> fileFingerprints = new LinkedHashMap<>();
        jdbc.query("SELECT file_name, format, content FROM service_config_file " +
                        "WHERE service_name IN ('application', ?) " +
                        "ORDER BY CASE service_name WHEN 'application' THEN 0 ELSE 1 END, file_name",
                rs -> {
                    fileFingerprints.putAll(ConfigFileParser.parse(
                            rs.getString("file_name"), rs.getString("format"), rs.getString("content")));
                }, serviceName);
        return fileFingerprints;
    }

    private String fingerprint(Map<String, Object> configs) {
        return String.join(";", configs.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .toList());
    }

    /**
     * 替换 Environment 中的 mysqlRegistry 属性源，并广播变更的配置键。
     */
    private void updateEnvironment(Map<String, Object> configs) {
        MutablePropertySources sources = environment.getPropertySources();
        sources.remove(MysqlConfigEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
        sources.addFirst(new MapPropertySource(MysqlConfigEnvironmentPostProcessor.PROPERTY_SOURCE_NAME, configs));
        publisher.publishEvent(new EnvironmentChangeEvent(this, Set.copyOf(configs.keySet())));
    }

}
