# hakira-cloud-project — 总体设计文档

> **项目名称：** hakira-cloud-project（原名 Elysia）
> **定位：** 会计账簿数据处理集成系统（复式记账）
> **技术栈：** Spring Cloud Alibaba 微服务
> **文档版本：** v1.0 · 2026-08-14

---

## 目录

1. [项目全景](#1-项目全景)
2. [技术栈总览](#2-技术栈总览)
3. [系统架构](#3-系统架构)
4. [模块职责划分](#4-模块职责划分)
5. [核心业务数据流](#5-核心业务数据流)
6. [分阶段实施路线](#6-分阶段实施路线)
7. [部署架构](#7-部署架构)
8. [项目结构](#8-项目结构)

---

## 1. 项目全景

### 1.1 项目目标

将原 Elysia 单体/杂乱项目重构为**会计账簿数据处理集成系统**，以**复式记账**为核心定位，构建一套基于 Spring Cloud Alibaba 的微服务架构，覆盖从「凭证录入 → 分录记账 → 库存核算 → 审批流 → 批处理对账 → 大数据分析」的完整会计数据处理链路。

### 1.2 核心需求

| 需求 | 说明 |
|------|------|
| 复式记账 | 会计分录借贷平衡校验，一借多贷/一贷多借 |
| 用户认证 | JWT 令牌认证，数据库用户 + BCrypt 密码哈希 |
| 库存核算 | 入库/出库流水 + 当前库存快照，乐观锁防并发 |
| 审批流 | Flowable 工作流引擎，凭证审批 |
| 批处理 | Quartz 定时调度 + Spring Batch 批量对账 |
| 数据持久化 | MySQL 分区表存储，会计数据不丢失 |
| 大数据分析 | Flink 流式 + Spark 批式分析（规划中） |

### 1.3 设计原则

| 原则 | 说明 |
|------|------|
| 微服务拆分 | 按业务域拆分为独立服务，gateway 统一入口 |
| 强一致性 | 会计数据用 RocketMQ 保证事务/顺序/延迟消息 |
| 数据持久化 | 会计流水落 MySQL，分区表按月归档 |
| 安全第一 | JWT + BCrypt，生产禁用明文密码 |
| 兼容演进 | 编译目标 17，实际 JDK 21 向下兼容 |

---

## 2. 技术栈总览

| 层级 | 技术选型 | 版本 | 用途 |
|------|---------|------|------|
| 语言 | Java | 17（JDK 21 编译） | 微服务开发 |
| 框架 | Spring Boot | 3.2.0 | 应用框架 |
| 微服务 | Spring Cloud Alibaba | 2023.0.1.0 | 微服务治理 |
| 注册/配置中心 | Nacos | v3.2.3 | 服务注册 + 配置管理 |
| 网关 | Spring Cloud Gateway | — | 统一路由 + JWT 校验 |
| 认证 | Spring Security + JWT | — | 登录认证 + 令牌签发 |
| 限流熔断 | Sentinel | — | 流控降级 |
| 消息队列 | RocketMQ | 5.0.0 | 会计事务/顺序/延迟消息 |
| 远程调用 | OpenFeign | — | 服务间调用 |
| ORM | MyBatis / MyBatis-Plus | 3.0.0 / 3.5.5 | 数据访问 |
| 数据库 | MySQL | 8.0 | 会计数据存储 |
| 审批流 | Flowable | — | 工作流引擎 |
| 批处理 | Spring Batch + Quartz | — | 批量对账 + 定时调度 |
| 大数据 | Flink / Spark | — | 流式/批式分析（规划中） |

---

## 3. 系统架构

### 3.1 微服务架构图

```
                          ┌─────────────────────────────┐
                          │   hakira-ledger-gateway      │  :9000
                          │   统一路由 + JWT 校验          │
                          └──────────────┬──────────────┘
                                         │ 路由转发
        ┌────────────────┬───────────────┼───────────────┬────────────────┐
        ▼                ▼               ▼               ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│     auth     │  │    entry     │  │    stock     │  │   workflow   │  │     task     │
│  认证中心     │  │  分录服务    │  │  库存服务    │  │  审批流服务   │  │  批处理服务   │
│  :9010       │  │  :9020       │  │  :9030       │  │  (Flowable)  │  │  (Quartz+    │
│ JWT+BCrypt  │  │ 复式记账     │  │ 入出库+快照  │  │              │  │   Batch)     │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │                 │                 │
       └─────────────────┴────────┬────────┴─────────────────┴─────────────────┘
                                  ▼
                    ┌─────────────────────────────┐
                    │         MySQL 8.0            │  :3307
                    │  hakira_nacos / hakira_auth  │
                    │  hakira_ledger（5 张分区表）  │
                    └─────────────────────────────┘

                    ┌─────────────────────────────┐
                    │      Nacos v3.2.3           │  :8848(API) :9848(gRPC) :8088(控制台)
                    │   注册中心 + 配置中心（内置存储）│
                    └─────────────────────────────┘
```

### 3.2 服务调用关系

```
gateway ──JWT校验──> auth（签发/校验令牌）
gateway ──路由──> entry / stock / workflow / task
entry  ──OpenFeign──> auth（可选：用户校验）
workflow ──> entry（审批通过后记账）
task ──> entry / stock（对账时读分录/库存）
```

---

## 4. 模块职责划分

| 模块 | 前缀 | 职责 |
|------|------|------|
| `hakira-common` | — | 公共库：Result 返回体、BizException、IdGenerator、工具类 |
| `hakira-fury` | — | Fury 序列化（实验性） |
| `hakira-ledger-api` | ledger | 接口定义 + DTO（IEntryService、IStockService 等） |
| `hakira-ledger-gateway` | ledger | 纯路由 + JWT 校验 |
| `hakira-ledger-auth` | ledger | 认证中心：登录、用户管理、JWT 签发 |
| `hakira-ledger-entry` | ledger | 分录服务：复式记账（借贷平衡 + 科目校验） |
| `hakira-ledger-stock` | ledger | 库存服务：入出库流水 + 快照 |
| `hakira-ledger-config` | ledger | 配置中心 |
| `hakira-ledger-mq` | ledger | RocketMQ 独立模块（自动装配） |
| `hakira-ledger-workflow` | ledger | Flowable 审批流 |
| `hakira-ledger-flink` | ledger | Flink 流式分析（规划中） |
| `hakira-ledger-spark` | ledger | Spark 批式分析（规划中） |
| `hakira-ledger-task` | ledger | Quartz 调度 + Spring Batch 批处理 |

---

## 5. 核心业务数据流

### 5.1 分录记账流程

```
POST /entry/post（凭证录入）
        │
        ▼
借贷平衡校验（借方合计 = 贷方合计，不等抛 1001）
        │
        ▼
科目合法性校验（科目编码须在 account_subject 存在，否则抛 1004）
        │
        ▼
生成分录ID（雪花ID 20位）+ 解析记账日期
        │
        ▼
事务写入：journal_entry（头）+ journal_entry_line（行）
        │
        ▼
返回 JournalEntryResponse（POSTED）
```

### 5.2 库存核算流程

```
POST /stock/inbound（入库）
        │
        ▼
查快照 → 首次则 insert 快照，否则乐观锁 update（version+1）
        │
        ▼
insert stock_movement（INBOUND 流水）
        │
        ▼
返回 StockMovementResponse

POST /stock/outbound（出库）
        │
        ▼
查快照 → 库存不足抛 1002
        │
        ▼
乐观锁 update 快照（current_quantity -= quantity）
        │
        ▼
insert stock_movement（OUTBOUND 流水）
```

### 5.3 认证流程

```
POST /login（用户名+密码）
        │
        ▼
DBUserManager 查 hakira_auth.hakira_user
        │
        ▼
BCryptPasswordEncoder 匹配哈希密码
        │
        ▼
签发 JWT → 返回 token + roles
        │
        ▼
gateway 校验 JWT → 放行/401
```

---

## 6. 分阶段实施路线

```
Phase 1 ── 模块架构重构（market→ledger）                  ✅ 已完成
           │  gateway+auth 分离、RocketMQ 独立、JWT 认证
Phase 2 ── 会计复式记账核心业务（ledger-core）             ✅ 已完成
           │  分录/库存/复式记账业务逻辑
Phase 3 ── 审批流与批处理（Flowable + Quartz + Batch）     ✅ 已完成
           │  工作流审批 + 定时对账批处理
Phase 4 ── 基础设施部署与冒烟测试（MySQL + Nacos）         ✅ 已完成
           │  容器化部署 + 14 项冒烟测试
Phase 5 ── 数据持久化与安全优化                            ✅ 已完成
           │  异常处理器 + BCrypt + 内存换 DB（分区表）
Phase 6 ── 大数据分析层（Flink + Spark）                   📋 规划中
              └─ 流式/批式分析会计流水（DB 已具备数据源）
```

> 详细设计见各 Phase 的 `PHASE-N-*-DESIGN.md` / `-DETAILED-DESIGN.md`，
> 测试见 `PHASE-N-*-TEST-REPORT.md`。

---

## 7. 部署架构

### 7.1 基础设施

| 组件 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| MySQL | hakira_ledger_mysql | 3307→3306 | 会计账本数据库（8.0） |
| Nacos | hakira_ledger_nacos | 8848/9848/8088→8080 | 注册/配置中心（内置 derby） |

### 7.2 应用服务端口

| 服务 | 端口 |
|------|------|
| gateway | 9000 |
| auth | 9010 |
| entry | 9020 |
| stock | 9030 |

### 7.3 部署方式

- 基础设施：`file/docker-compose-infra.yml`（Docker Compose）
- 应用服务：`mvn package` 产出 fat jar，`java -jar` 启动
- 服务不设 context-path，由 gateway 统一路由

---

## 8. 项目结构

```
hakira-cloud-project/
├── hakira-common/          # 公共库
├── hakira-fury/            # Fury 序列化（实验）
├── hakira-ledger-api/      # 接口定义 + DTO
├── hakira-ledger-gateway/  # 网关
├── hakira-ledger-auth/     # 认证中心
├── hakira-ledger-entry/    # 分录服务
├── hakira-ledger-stock/    # 库存服务
├── hakira-ledger-config/   # 配置中心
├── hakira-ledger-mq/       # RocketMQ
├── hakira-ledger-workflow/ # Flowable 审批流
├── hakira-ledger-flink/    # Flink（规划）
├── hakira-ledger-spark/    # Spark（规划）
├── hakira-ledger-task/     # Quartz + Batch
├── file/                   # 部署编排 + SQL（见 目录约定.md）
└── docs/                   # 设计/详细设计/接口/测试文档（本目录）
```
