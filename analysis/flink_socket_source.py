#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Flink 流式作业的 socket 数据源（Phase 6 验证辅助脚本）
功能：监听 19000 端口，接受 Flink 作业（SocketWindowWordCount）连接后，
     推送模拟的库存流水事件（itemCode），供 Flink 实时窗口统计。

使用：先启动本脚本（后台），再提交 Flink SocketWindowWordCount 作业
      （--hostname <宿主机docker网关> --port 19000）。

关键点：Flink 的 socketTextStream 是【客户端】主动连接本服务器；
        不能再用 nc -lk（单连接 backlog 导致第二个客户端卡住）。
"""
import socket
import time

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(("0.0.0.0", 19000))
s.listen(5)
print("[socket源] 监听 19000，等待 Flink 连接...", flush=True)

conn, addr = s.accept()
print(f"[socket源] Flink 已连接: {addr}", flush=True)

# 模拟库存流水事件（itemCode），每 0.5 秒一条
words = ["itemA", "itemB", "itemA", "itemC", "itemA",
         "itemB", "itemC", "itemB", "itemA", "itemC"]
try:
    for w in words:
        conn.sendall((w + "\n").encode())
        time.sleep(0.5)
    print(f"[socket源] 已发送 {len(words)} 条流水事件，保持连接 60s...", flush=True)
    time.sleep(60)
except BrokenPipeError:
    print("[socket源] Flink 连接断开", flush=True)
finally:
    conn.close()
    s.close()
