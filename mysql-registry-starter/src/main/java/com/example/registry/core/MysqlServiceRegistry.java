package com.example.registry.core;

import com.example.registry.MysqlRegistryProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 服务注册/注销/心跳/剔除，直接操作 service_instance 表。
 */
public class MysqlServiceRegistry implements ServiceRegistry<Registration> {

    private final JdbcTemplate jdbc;
    private final MysqlRegistryProperties properties;

    public MysqlServiceRegistry(JdbcTemplate jdbc, MysqlRegistryProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    /**
     * 注册实例，幂等：重复注册仅续约。
     */
    @Override
    public void register(Registration registration) {
        jdbc.update("""
                INSERT INTO service_instance (instance_id, service_name, host, port, status, last_heartbeat)
                VALUES (?, ?, ?, ?, 'UP', NOW(3))
                ON DUPLICATE KEY UPDATE status = 'UP', last_heartbeat = NOW(3), host = VALUES(host), port = VALUES(port)
                """,
                registration.getInstanceId(), registration.getServiceId(),
                registration.getHost(), registration.getPort());
    }

    /**
     * 注销实例，直接删除记录。
     */
    @Override
    public void deregister(Registration registration) {
        jdbc.update("DELETE FROM service_instance WHERE instance_id = ?", registration.getInstanceId());
    }

    /**
     * 心跳续约，被剔除后自动重新注册。
     */
    public void heartbeat(Registration registration) {
        int rows = jdbc.update("""
                UPDATE service_instance SET last_heartbeat = NOW(3)
                WHERE instance_id = ? AND status = 'UP'
                """, registration.getInstanceId());
        if (rows == 0) {
            register(registration);
        }
    }

    /**
     * 将心跳超时的实例标记为 DOWN。
     */
    public void evictStaleInstances() {
        long seconds = properties.getHeartbeatTimeout() / 1000;
        jdbc.update("UPDATE service_instance SET status = 'DOWN' WHERE status = 'UP' AND last_heartbeat < NOW(3) - INTERVAL " + seconds + " SECOND");
    }

    /**
     * 释放资源，无额外资源需要释放。
     */
    @Override
    public void close() {
    }

    /**
     * 手动更新实例状态。
     */
    @Override
    public void setStatus(Registration registration, String status) {
        jdbc.update("UPDATE service_instance SET status = ? WHERE instance_id = ?", status, registration.getInstanceId());
    }

    /**
     * 查询实例当前状态。
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getStatus(Registration registration) {
        return (T) jdbc.queryForObject("SELECT status FROM service_instance WHERE instance_id = ?", String.class, registration.getInstanceId());
    }

}
