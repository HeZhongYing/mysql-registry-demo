#!/bin/bash
# Linux 部署启动脚本
# 用法: ./start.sh <服务名> [额外Spring参数...]
# 示例: MYSQL_HOST=192.168.1.100 ./start.sh user-service
#       MYSQL_HOST=192.168.1.100 ./start.sh user-service --server.port=8085

set -e

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVICE=$1
shift || true

if [ -z "$SERVICE" ]; then
    echo "用法: $0 <gateway-service|user-service|order-service|product-service|console-service> [额外Spring参数]"
    exit 1
fi

JAR=$(ls "$APP_DIR"/../dist/"$SERVICE"-*.jar 2>/dev/null || ls "$APP_DIR"/../"$SERVICE"/target/"$SERVICE"-*.jar 2>/dev/null || ls "$APP_DIR/$SERVICE"-*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
    echo "未找到 $SERVICE 的 jar，请先在开发机执行 mvn package 并上传 dist 目录"
    exit 1
fi

LOG_DIR="$APP_DIR/logs"
mkdir -p "$LOG_DIR"

# 跨环境部署时必须指定 MySQL 所在机器的 IP
if [ -z "$MYSQL_HOST" ]; then
    echo "提示: 未设置 MYSQL_HOST，将使用默认 127.0.0.1"
fi

echo "启动 $JAR"
nohup java -Dfile.encoding=UTF-8 ${JAVA_OPTS:-} -jar "$JAR" "$@" > "$LOG_DIR/$SERVICE.log" 2>&1 &
echo "$!" > "$LOG_DIR/$SERVICE.pid"
echo "PID: $(cat "$LOG_DIR/$SERVICE.pid")，日志: $LOG_DIR/$SERVICE.log"
