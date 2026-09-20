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
    weight          INT          NOT NULL DEFAULT 100 COMMENT '负载均衡权重',
    status          VARCHAR(16)  NOT NULL DEFAULT 'UP' COMMENT '状态：UP 在线 / DOWN 心跳超时 / OFFLINE 手动下线',
    metadata        VARCHAR(1024)         DEFAULT NULL COMMENT '扩展元数据 JSON',
    last_heartbeat  DATETIME(3)  NOT NULL COMMENT '最后心跳时间',
    registered_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_instance_id (instance_id),
    KEY idx_service_status (service_name, status),
    KEY idx_last_heartbeat (last_heartbeat)
) ENGINE = InnoDB COMMENT '服务实例表';

-- 配置文件表：配置中心（Nacos 模型，一份配置文件为一个管理单元）
CREATE TABLE IF NOT EXISTS service_config_file (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    service_name VARCHAR(128) NOT NULL DEFAULT 'application' COMMENT '归属服务，application 表示全局',
    file_name    VARCHAR(255) NOT NULL COMMENT '配置文件名，如 user-service.yaml',
    format       VARCHAR(16)  NOT NULL DEFAULT 'yaml' COMMENT '格式：yaml / properties',
    content      MEDIUMTEXT COMMENT '配置文件全文',
    version      BIGINT       NOT NULL DEFAULT 1 COMMENT '版本号，每次变更 +1',
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_file (service_name, file_name)
) ENGINE = InnoDB COMMENT '配置文件表';

-- 配置变更历史表：审计 + 动态刷新辅助
CREATE TABLE IF NOT EXISTS config_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    service_name VARCHAR(128) NOT NULL COMMENT '归属服务',
    file_name    VARCHAR(255) NOT NULL COMMENT '配置文件名',
    old_content  MEDIUMTEXT COMMENT '旧内容',
    new_content  MEDIUMTEXT COMMENT '新内容',
    version      BIGINT       NOT NULL COMMENT '变更后版本号',
    changed_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_service_file_time (service_name, file_name, changed_at)
) ENGINE = InnoDB COMMENT '配置变更历史表';

-- 演示用初始配置文件（验证动态刷新）
INSERT INTO service_config_file (service_name, file_name, format, content) VALUES
    ('application',  'application.yaml', 'yaml', 'common:\n  name: mysql-registry-demo\n'),
    ('user-service', 'user-service.yaml', 'yaml', 'user:\n  greeting: hello-from-mysql\n')
ON DUPLICATE KEY UPDATE file_name = file_name;
