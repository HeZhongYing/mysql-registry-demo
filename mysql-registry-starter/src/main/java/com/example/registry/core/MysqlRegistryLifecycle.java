package com.example.registry.core;

import com.example.registry.MysqlRegistryProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.InetAddress;

/**
 * 实例生命周期：启动后注册，周期性心跳续约、剔除扫描，关闭时注销。
 */
public class MysqlRegistryLifecycle implements ApplicationListener<WebServerInitializedEvent> {

    private static final Log log = LogFactory.getLog(MysqlRegistryLifecycle.class);

    private final MysqlServiceRegistry registry;
    private final MysqlRegistryProperties properties;
    private final Environment environment;

    private volatile Registration registration;

    public MysqlRegistryLifecycle(MysqlServiceRegistry registry, MysqlRegistryProperties properties, Environment environment) {
        this.registry = registry;
        this.properties = properties;
        this.environment = environment;
    }

    /**
     * Web 容器就绪后注册（Tomcat 与 Netty 都会发布该事件）。
     * 注册 IP 优先取 mysql-registry.instance-ip 配置，未配置则自动探测。
     */
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (registration != null) {
            return;
        }
        try {
            String serviceId = environment.getProperty("spring.application.name", "unknown");
            String configuredIp = properties.getInstanceIp();
            String host = (configuredIp == null || configuredIp.isBlank())
                    ? InetAddress.getLocalHost().getHostAddress()
                    : configuredIp.trim();
            int port = event.getWebServer().getPort();
            registration = new MysqlRegistration(serviceId, host, port, java.util.Map.of("weight", "100"));
            registry.register(registration);
            log.info("已注册到 MySQL 注册中心: " + registration.getInstanceId() + " (" + serviceId + ")");
        } catch (Exception e) {
            log.error("注册失败", e);
        }
    }

    /**
     * 心跳续约。
     */
    @Scheduled(fixedDelayString = "${mysql-registry.heartbeat-interval:10000}")
    public void heartbeat() {
        Registration r = registration;
        if (r != null) {
            registry.heartbeat(r);
        }
    }

    /**
     * 剔除心跳超时实例。
     */
    @Scheduled(fixedDelayString = "${mysql-registry.evict-interval:10000}")
    public void evict() {
        registry.evictStaleInstances();
    }

    /**
     * 应用关闭时注销实例。
     */
    @org.springframework.context.event.EventListener
    public void onClosed(ContextClosedEvent event) {
        Registration r = registration;
        if (r != null) {
            registry.deregister(r);
            log.info("已从 MySQL 注册中心注销: " + r.getInstanceId());
        }
    }

}
