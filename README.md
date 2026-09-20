# mysql-registry-demo

使用 MySQL 作为注册中心的微服务研究项目。

## 目标

- 使用 MySQL 实现：服务注册、心跳/剔除、服务发现、配置中心（支持动态刷新）
- 服务间调用使用 Feign（走自定义 DiscoveryClient + LoadBalancer）
- 注册/发现/配置逻辑统一封装在 `mysql-registry-starter` 模块

## 模块

| 模块 | 说明 | 端口 |
| --- | --- | --- |
| mysql-registry-starter | 注册中心 + 配置中心实现（Spring Boot AutoConfiguration） | 无 |
| gateway-service | Spring Cloud Gateway，路由走 MySQL 注册中心 | 8080 |
| user-service | 用户服务 | 8081 |
| order-service | 订单服务 | 8082 |
| product-service | 商品服务 | 8083 |
| console-service | 注册中心可视化控制台，提供查询 API 并托管前端页面 | 8084 |
| frontend | Vue3 + Vite 前端工程，构建产物输出到 console-service | 5173(dev) |

## 技术栈

- JDK 21
- Spring Boot 3.x
- Spring Cloud
- Maven 3.9
- Feign
- MySQL 8

## 数据库

单个 `registry_demo` 库，4 个服务共用。

## 任务进度

- [x] 任务一：注册中心作用分析
- [x] 任务二：脚手架
- [x] 任务三：建表
- [x] 任务四：注册中心实现与改造
- [x] 任务五：可视化控制台（`http://localhost:8080/console/`）
