#!/bin/bash
# 停止服务脚本（优雅停止，触发注销）
# 用法: ./stop.sh <服务名>

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "用法: $0 <服务名>"
    exit 1
fi

PID_FILE="$APP_DIR/logs/$SERVICE.pid"
if [ ! -f "$PID_FILE" ]; then
    echo "未找到 $SERVICE 的 PID 文件"
    exit 1
fi

PID=$(cat "$PID_FILE")
if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    echo "已发送停止信号到 $SERVICE (PID $PID)，注销后进程退出"
else
    echo "$SERVICE 未在运行"
fi
rm -f "$PID_FILE"
