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

单个 `registry_demo` 库，所有服务共用。

## 跨环境部署（Windows / Linux 连同一 MySQL）

1. **MySQL 远程访问**（MySQL 所在机器执行一次）：
   ```sql
   CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '123456';
   GRANT ALL PRIVILEGES ON registry_demo.* TO 'root'@'%';
   FLUSH PRIVILEGES;
   ```
   并确认防火墙放行 3306。

2. **打包**（开发机执行）：`mvn package -DskipTests`，全部服务 jar 自动收拢到根目录 `dist/`，连同 `deploy/` 一起上传 Linux

3. **Linux 启动**（jar 与 `deploy/` 放同一目录结构，或把 jar 放到 deploy 上级对应服务目录）：
   ```bash
   # MYSQL_HOST 指向 MySQL 所在机器；多网卡时可加 --mysql-registry.instance-ip=本机可被访问的IP
   MYSQL_HOST=<MySQL机器IP> ./start.sh user-service
   MYSQL_HOST=<MySQL机器IP> ./start.sh user-service --server.port=8085   # 第二实例
   ./stop.sh user-service
   ```

4. **验证**：控制台出现另一环境的实例 IP；网关调用 `fromPort` 分流到两个环境的实例。

配置均支持环境变量覆盖：`MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USER` / `MYSQL_PASSWORD`。

## 任务进度

- [x] 任务一：注册中心作用分析
- [x] 任务二：脚手架
- [x] 任务三：建表
- [x] 任务四：注册中心实现与改造
- [x] 任务五：可视化控制台（`http://localhost:8080/console/`）
- [x] 任务六：页面操作（实例下线/上线、权重调整、配置增删改）
