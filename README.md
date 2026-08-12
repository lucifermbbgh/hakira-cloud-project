# hakira-cloud-project — 会计账簿数据处理集成系统

## 项目定位

基于 **Spring Cloud Alibaba** 微服务架构的会计账簿（Ledger）数据处理平台。核心业务围绕复式记账（Double-Entry Bookkeeping）展开，涵盖账务分录录入、库存台账、工作流审批、实时合规自检、批量报表生成等能力。

**技术栈基线**：Java 17 · Spring Boot 3.2 · Spring Cloud 2023.0 · Spring Cloud Alibaba 2022.0.0.0-RC2

---

## 模块架构

```
hakira-cloud-project/
│
├── 🧱 基础设施层（不加 ledger 前缀）
│   ├── hakira-common                   # 公共工具库（加密、ID生成、统一响应、枚举常量）
│   └── hakira-fury                     # [实验性] Apache Fury 序列化对象池
│
├── 🌐 网关与安全层
│   ├── hakira-ledger-gateway           # Spring Cloud Gateway 统一入口（路由、JWT校验、限流）
│   └── hakira-ledger-auth              # 认证鉴权中心（登录、OAuth2、用户/角色/菜单管理）
│
├── 📒 会计核心域
│   ├── hakira-ledger-api               # Feign 接口契约层（IEntryService / IStockService / DTO）
│   ├── hakira-ledger-entry             # 账务分录录入服务（借贷记账、流水管理）
│   ├── hakira-ledger-stock             # 库存台账服务（库存变动、盘点、EasyExcel 导入导出）
│   ├── hakira-ledger-config            # 会计域 Nacos 配置引导
│   └── hakira-ledger-mq                # RocketMQ 消息队列模块（生产者/消费者封装）
│
├── 📋 工作流层
│   └── hakira-ledger-workflow          # Flowable 工作流引擎（审批、双向核对、自动签字）
│
├── 📊 数据分析层
│   ├── hakira-ledger-flink             # Flink 实时流处理（借贷平衡校验、大额预警、异常检测）
│   └── hakira-ledger-spark             # Spark 批处理（资产负债表、利润表、现金流量表、指标分析）
│
└── ⏰ 调度层
    └── hakira-ledger-task              # Quartz 定时触发 + Spring Batch 批量作业（Job 依赖链）
```

---

## 核心数据流

```
外部请求 → Nginx :80
              │
              ▼
     hakira-ledger-gateway（JWT 校验 + 路由）
         │                    │
         │ /login /oauth2     │ /api/**
         ▼                    ▼
  hakira-ledger-auth    ┌─────┴─────┬──────────┬──────────┐
  （登录/Token签发）     ▼           ▼          ▼          ▼
                     entry       stock     workflow   data-*
                        │           │          │
                        └─────┬─────┘          │
                              ▼                │
                     hakira-ledger-mq          │
                       (RocketMQ)              │
                              │                │
                    ┌─────────┴─────────┐      │
                    ▼                   ▼      │
             ledger-flink         ledger-spark │
            （实时合规）           （批处理报表） │
                    │                   │      │
                    └───────────────────┴──────┘
```

---

## RocketMQ Topic 规划

| Topic | 用途 |
|-------|------|
| `hakira-ledger-entry-topic` | 账务分录事件（借贷流水） |
| `hakira-ledger-stock-topic` | 库存变动事件 |
| `hakira-ledger-reconcile-topic` | 双向核对请求/响应 |
| `hakira-ledger-report-topic` | 报表生成触发 |
| `hakira-data-alert-topic` | 实时合规预警通知 |

---

## 业务流程

```
出纳收支 → 原始凭证登记 → 借贷分录录入 → 双向核对
                                          ├─ 一致 → 复核审批 → 会计报表 → 自动签字
                                          └─ 不一致 → 异常处理 → 人工介入
```

---

## 技术栈详表

| 层次 | 技术 | 模块 |
|------|------|------|
| 服务注册/配置 | Nacos | 全局 |
| API 网关 | Spring Cloud Gateway | `hakira-ledger-gateway` |
| 认证鉴权 | Spring Security + OAuth2 + JWT | `hakira-ledger-auth` |
| RPC 调用 | OpenFeign + LoadBalancer | `hakira-ledger-api` / 各服务 |
| 流量控制 | Sentinel + Nacos 持久化 | 各服务 |
| 消息队列 | RocketMQ | `hakira-ledger-mq` |
| 工作流 | Flowable | `hakira-ledger-workflow` |
| 实时计算 | Apache Flink | `hakira-ledger-flink` |
| 批处理 | Apache Spark + Spring Batch | `hakira-ledger-spark` / `hakira-ledger-task` |
| 定时调度 | Quartz | `hakira-ledger-task` |
| ORM | MyBatis + MyBatis-Plus + tk.mybatis | 各数据服务 |
| 数据库 | MySQL 8.0 + Druid 连接池 | 各数据服务 |
| 缓存 | Redis | 各服务 |
| 序列化 | FastJSON2 / Jackson / Gson / Apache Fury | `hakira-common` / `hakira-fury` |
| Excel | EasyExcel | `hakira-ledger-stock` |
| 视图 | Thymeleaf | `hakira-ledger-auth` |
| API 文档 | Knife4j | `hakira-common` |

---

## 部署

```bash
# 基础设施
docker-compose -f file/docker-compose.yml up -d
# 启动：MySQL + Nacos + Nginx
```

---

## 版本历史

| 版本 | 分支 | 说明 |
|------|------|------|
| 2.0.4 | `release` (锁定) | market 电商命名时期，已存档 |
| 3.0.0 | `release-202608-remake` | ledger 会计账簿架构重构 |
