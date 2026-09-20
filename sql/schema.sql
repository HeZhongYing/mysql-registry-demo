-- MySQL 注册中心表结构
-- 库：registry_demo，4 个服务共用

CREATE DATABASE IF NOT EXISTS registry_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE registry_demo;

-- 服务实例表：注册 + 心跳 + 发现
CREATE TABLE IF NOT EXISTS service_instance (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    instance_id     VARCHAR(64)  NOT NULL COMMENT '实例唯一标识（host:port）',
    service_name    VARCHAR(128) NOT NULL COMMENT '服务名',
    host            VARCHAR(64)  NOT NULL COMMENT '实例 IP',
    port            INT          NOT NULL COMMENT '实例端口',
    status          VARCHAR(16)  NOT NULL DEFAULT 'UP' COMMENT '状态：UP / DOWN',
    metadata        VARCHAR(1024)         DEFAULT NULL COMMENT '扩展元数据 JSON',
    last_heartbeat  DATETIME(3)  NOT NULL COMMENT '最后心跳时间',
    registered_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_instance_id (instance_id),
    KEY idx_service_status (service_name, status),
    KEY idx_last_heartbeat (last_heartbeat)
) ENGINE = InnoDB COMMENT '服务实例表';

-- 配置表：配置中心
CREATE TABLE IF NOT EXISTS service_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    service_name VARCHAR(128) NOT NULL DEFAULT 'application' COMMENT '归属服务，application 表示全局',
    config_key   VARCHAR(255) NOT NULL COMMENT '配置键',
    config_value TEXT                  DEFAULT NULL COMMENT '配置值',
    version      BIGINT       NOT NULL DEFAULT 1 COMMENT '版本号，每次变更 +1',
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_key (service_name, config_key)
) ENGINE = InnoDB COMMENT '配置表';

-- 配置变更历史表：审计 + 动态刷新辅助
CREATE TABLE IF NOT EXISTS config_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    service_name VARCHAR(128) NOT NULL COMMENT '归属服务',
    config_key   VARCHAR(255) NOT NULL COMMENT '配置键',
    old_value    TEXT COMMENT '旧值',
    new_value    TEXT COMMENT '新值',
    version      BIGINT       NOT NULL COMMENT '变更后版本号',
    changed_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_service_key_time (service_name, config_key, changed_at)
) ENGINE = InnoDB COMMENT '配置变更历史表';

-- 演示用初始配置（任务四验证动态刷新）
INSERT INTO service_config (service_name, config_key, config_value) VALUES
    ('user-service', 'user.greeting', 'hello-from-mysql'),
    ('application',  'common.name',   'mysql-registry-demo')
ON DUPLICATE KEY UPDATE config_key = config_key;
