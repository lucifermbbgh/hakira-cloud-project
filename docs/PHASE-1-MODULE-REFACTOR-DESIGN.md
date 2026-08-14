# hakira-cloud-project — Phase 1 模块架构重构方案设计

> **阶段：** Phase 1 · 模块架构重构（market → ledger）
> **状态：** ✅ 已完成
> **关联提交：** ecd23a61 · 33ce32da · bb884f85 · b3347764

---

## 一、阶段目标

将原 Elysia（market 商城）项目重构为会计账簿（ledger）微服务架构，完成「拆模块、理依赖、定规范」三项基础工作，为后续会计业务开发铺路。

## 二、阶段范围

| 工作项 | 内容 |
|--------|------|
| 大规模模块重构 | market 商城 → ledger 会计账簿，13 模块结构确立 |
| 网关拆分 | gateway（纯路由 + JWT 校验）+ auth（认证中心）分离 |
| 消息队列提取 | RocketMQ 独立成 `hakira-ledger-mq` 模块，自动装配 |
| 认证体系 | Gateway 路由配置 + Spring Security JWT 令牌认证 |
| 工具链兼容 | Lombok 1.18.26 → 1.18.34 兼容 JDK 21 |

## 三、设计要点

### 3.1 模块命名规范

除 `hakira-common`、`hakira-fury` 外，所有业务模块统一加 `ledger` 前缀，包名统一 `com.hakira.ledger.*`。

### 3.2 网关拆分决策

| 决策 | 方案 |
|------|------|
| gateway | 纯路由 + JWT 校验，不承载业务 |
| auth | 认证中心：登录、用户管理、JWT 签发 |
| 理由 | 关注点分离，认证逻辑独立演进 |

### 3.3 MQ 选型：RocketMQ 而非 Kafka

会计业务要求**强一致、事务消息、顺序消息、延迟消息**，RocketMQ 原生支持，Kafka 侧重吞吐不契合会计场景。RocketMQ 独立成模块，通过 Spring Boot 自动装配按需引入。

### 3.4 任务调度不拆分

task 单模块承载 Quartz（定时调度）+ Spring Batch（批处理），不进一步拆分，降低维护成本。

## 四、模块依赖关系

```
hakira-ledger-gateway ──> hakira-common, hakira-ledger-api
hakira-ledger-auth    ──> hakira-common, hakira-ledger-api, hakira-ledger-mq
hakira-ledger-entry   ──> hakira-common, hakira-ledger-api
hakira-ledger-stock   ──> hakira-common, hakira-ledger-api
hakira-ledger-task    ──> hakira-common, hakira-ledger-api
hakira-ledger-workflow──> hakira-common, hakira-ledger-api
```

---

## 五、阶段验收标准

- [x] 13 模块结构建立，编译通过
- [x] gateway/auth 分离，路由 + JWT 可跑通
- [x] RocketMQ 独立模块，自动装配生效
- [x] JDK 21 下 Lombok 编译无警告
