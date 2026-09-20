package com.example.registry;

import com.example.registry.config.MysqlConfigRefresher;
import com.example.registry.config.MysqlConfigRefresher;
import com.example.registry.core.MysqlDiscoveryClient;
import com.example.registry.core.MysqlReactiveDiscoveryClient;
import com.example.registry.core.MysqlRegistryLifecycle;
import com.example.registry.core.MysqlServiceRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MySQL 注册中心自动配置：注册、发现、心跳剔除、配置动态刷新。
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(MysqlRegistryProperties.class)
@ConditionalOnProperty(prefix = "mysql-registry", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MysqlRegistryAutoConfiguration {

    @Bean
    public MysqlServiceRegistry mysqlServiceRegistry(JdbcTemplate jdbc, MysqlRegistryProperties properties) {
        return new MysqlServiceRegistry(jdbc, properties);
    }

    @Bean
    public MysqlDiscoveryClient mysqlDiscoveryClient(JdbcTemplate jdbc, MysqlRegistryProperties properties) {
        return new MysqlDiscoveryClient(jdbc, properties);
    }

    /**
     * 响应式包装，仅在 reactor 存在（如 Gateway）时装配。
     */
    @Bean
    @ConditionalOnClass(reactor.core.publisher.Flux.class)
    public MysqlReactiveDiscoveryClient mysqlReactiveDiscoveryClient(MysqlDiscoveryClient delegate) {
        return new MysqlReactiveDiscoveryClient(delegate);
    }

    @Bean
    public MysqlRegistryLifecycle mysqlRegistryLifecycle(MysqlServiceRegistry registry, MysqlRegistryProperties properties, Environment environment) {
        return new MysqlRegistryLifecycle(registry, properties, environment);
    }

    @Bean
    public MysqlConfigRefresher mysqlConfigRefresher(JdbcTemplate jdbc, ConfigurableApplicationContext context) {
        return new MysqlConfigRefresher(jdbc, context.getEnvironment(), context);
    }

}
