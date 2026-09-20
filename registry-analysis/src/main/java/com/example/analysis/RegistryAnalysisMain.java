package com.example.analysis;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务一：分析微服务架构中注册中心充当的作用。
 * 启动时控制台打印分析结果。
 */
public class RegistryAnalysisMain {

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        String banner = """
                ====================================================================
                                 微服务架构中「注册中心」作用分析
                ====================================================================
                生成时间：%s
                --------------------------------------------------------------------

                一、什么是注册中心
                  注册中心是微服务架构中负责「服务元数据存储与查询」的中心化组件。
                  它维护一张「服务名 -> 可用实例列表」的映射表，让服务间调用从
                  硬编码 IP/端口 解耦为「按服务名寻址」。

                --------------------------------------------------------------------

                二、注册中心的核心职责

                  1. 服务注册（Register）
                     服务启动时把自己的元数据（服务名、IP、端口、实例ID、版本、
                     权重、元数据）写入注册中心。

                  2. 心跳续约（Heartbeat / Renew）
                     服务周期性更新自己的「最后心跳时间戳」，告诉注册中心「我还活着」。

                  3. 健康检查与剔除（Health Check / Evict）
                     注册中心周期性扫描，对超过 TTL 没有心跳的实例标记为不可用或剔除。

                  4. 服务发现（Discovery）
                     消费方按服务名查询可用实例列表，用于客户端负载均衡选择目标实例。

                  5. 配置中心（Config Center）
                     集中管理各服务的配置，支持动态刷新（修改后无需重启即可生效）。

                  6. 服务变更通知（Watch / Subscribe，本次实现暂不包含）
                     实例上下线时主动通知订阅者，减少轮询延迟。

                --------------------------------------------------------------------

                三、注册中心在调用链中的位置

                  [调用方] -> [注册中心：查可用实例] -> [客户端负载均衡选一个] -> [Feign/HTTP] -> [被调方]
                                   ^                                                              |
                                   |--- 心跳续约 <-----------------------------------------------|

                  没有注册中心时：调用方需要硬编码 IP/端口，扩容/迁移/弹性伸缩都极痛。
                  有注册中心后：实例上下线对调用方透明，调用方只认「服务名」。

                --------------------------------------------------------------------

                四、为什么用 MySQL 做注册中心（研究价值）

                  - 主流注册中心（Eureka/Nacos/Consul/ZK）内部本质都是「一张实例表 + 一份配置」。
                    用 MySQL 直接落地这张表，可以最直观地理解注册中心的数据模型。
                  - MySQL 自带 ACID，写注册/心跳/配置变更天然有事务保证。
                  - 缺点：MySQL 不擅长「高频心跳写入」和「实例变更实时推送」，
                    生产场景需配合 Redis 缓存或 Pub/Sub 通知，本研究的 Redis 方案即为此预留。

                --------------------------------------------------------------------

                五、本研究将要落地的能力（其他暂不考虑）

                  [x] 服务注册：启动时写 service_instance
                  [x] 心跳续约：定时 UPDATE last_heartbeat
                  [x] 健康剔除：定时扫描，超时实例标记不可用
                  [x] 服务发现：提供 DiscoveryClient 实现，供 Feign 与 Gateway 使用
                  [x] 配置中心：config 表 + 定时轮询 + @RefreshScope 动态刷新
                  [-] 服务变更通知：暂不实现
                  [-] 多机房集群 / 高可用：暂不实现

                ====================================================================
                """.formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        System.out.println(banner);
    }

}
