package com.example.registry.core;

import com.example.registry.MysqlRegistryProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 阻塞式服务发现，供 Feign 与 LoadBalancer 使用。
 */
public class MysqlDiscoveryClient implements DiscoveryClient {

    private final JdbcTemplate jdbc;
    private final MysqlRegistryProperties properties;

    public MysqlDiscoveryClient(JdbcTemplate jdbc, MysqlRegistryProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    /**
     * 查询服务的可用实例，心跳超时与手动下线的实例不算可用。权重放入 metadata 供负载均衡使用。
     */
    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        long seconds = properties.getHeartbeatTimeout() / 1000;
        return jdbc.query("""
                SELECT service_name, host, port, weight FROM service_instance
                WHERE service_name = ? AND status = 'UP'
                  AND last_heartbeat > NOW(3) - INTERVAL %d SECOND
                """.formatted(seconds),
                (rs, i) -> new MysqlRegistration(
                        rs.getString("service_name"),
                        rs.getString("host"),
                        rs.getInt("port"),
                        java.util.Map.of("weight", String.valueOf(rs.getInt("weight")))),
                serviceId);
    }

    /**
     * 查询所有有存活实例的服务名。
     */
    @Override
    public List<String> getServices() {
        long seconds = properties.getHeartbeatTimeout() / 1000;
        return jdbc.queryForList("""
                SELECT DISTINCT service_name FROM service_instance
                WHERE status = 'UP' AND last_heartbeat > NOW(3) - INTERVAL %d SECOND
                """.formatted(seconds), String.class);
    }

    /**
     * 实现描述。
     */
    @Override
    public String description() {
        return "MySQL Registry DiscoveryClient";
    }

}
