package com.example.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MySQL 注册中心配置项。
 */
@ConfigurationProperties(prefix = "mysql-registry")
public class MysqlRegistryProperties {

    /** 是否启用注册中心 */
    private boolean enabled = true;

    /** 心跳续约间隔（毫秒） */
    private long heartbeatInterval = 10_000;

    /** 心跳超时（毫秒），超过视为实例不可用 */
    private long heartbeatTimeout = 30_000;

    /** 剔除扫描间隔（毫秒） */
    private long evictInterval = 10_000;

    /** 配置轮询间隔（毫秒） */
    private long configPollInterval = 5_000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public long getEvictInterval() {
        return evictInterval;
    }

    public void setEvictInterval(long evictInterval) {
        this.evictInterval = evictInterval;
    }

    public long getConfigPollInterval() {
        return configPollInterval;
    }

    public void setConfigPollInterval(long configPollInterval) {
        this.configPollInterval = configPollInterval;
    }

}
