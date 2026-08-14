# hakira-cloud-project — Phase 1 模块架构重构详细设计

> **阶段：** Phase 1 · 模块架构重构
> **状态：** ✅ 已完成

---

## 一、模块分层

| 模块 | 定位 | 关键职责 |
|------|------|---------|
| `hakira-common` | 公共库 | `Result` 返回体、`IdGeneratorUtil`、异常类、工具 |
| `hakira-fury` | 序列化 | Fury 序列化（实验性） |
| `hakira-ledger-api` | 接口契约 | `IEntryService`/`IStockService` 接口 + DTO |
| `hakira-ledger-gateway` | 网关 | 统一路由 + JWT 校验过滤器 |
| `hakira-ledger-auth` | 认证中心 | 登录、用户管理、JWT 签发 |
| `hakira-ledger-entry` | 分录服务 | 复式记账业务 |
| `hakira-ledger-stock` | 库存服务 | 入出库 + 快照 |
| `hakira-ledger-config` | 配置中心 | 配置管理 |
| `hakira-ledger-mq` | 消息队列 | RocketMQ 自动装配 |
| `hakira-ledger-workflow` | 审批流 | Flowable 引擎 |
| `hakira-ledger-flink` | 流式计算 | Flink（规划） |
| `hakira-ledger-spark` | 批式计算 | Spark（规划） |
| `hakira-ledger-task` | 批处理 | Quartz + Spring Batch |

## 二、网关与认证拆分设计

### 2.1 Gateway 职责

- 统一入口，路由转发到各业务服务
- JWT 过滤器校验令牌，无令牌/令牌失效返回 401
- 不承载认证业务逻辑

### 2.2 Auth 职责

- `LoginController`：登录页 + OAuth GitHub 登录
- `UserController`：用户注册/查询（`/user/register`、`/user/getList`）
- `WebSecurityConfig`：Spring Security 配置（后续 Phase 5 换 BCrypt）
- JWT 签发：登录成功 → 生成 token 返回

### 2.3 认证数据流

```
POST /login → UsernamePasswordAuthenticationFilter
    → DaoAuthenticationProvider → DBUserManager.loadUserByUsername
    → 密码匹配 → 签发 JWT → 返回 token + roles
```

## 三、RocketMQ 自动装配设计

`hakira-ledger-mq` 模块通过 Spring Boot 自动装配机制（`AutoConfiguration.imports`）暴露 RocketMQ 组件，业务模块按需引入依赖即可，无需显式配置。

> 注：后续发现 auth 模块的 RocketMQ 依赖为僵尸依赖（登录认证不涉及 MQ），Phase 4 清理。

## 四、Lombok / JDK 兼容

| 项 | 变更 |
|----|------|
| Lombok | 1.18.26 → 1.18.34 |
| 原因 | 旧版本在 JDK 21 下编译报错 |
| JDK | 编译目标 17，实际 JDK 21 向下兼容 |

## 五、关键配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 端口段 | 90xx | gateway 9000 / auth 9010 / entry 9020 / stock 9030 |
| 服务名 | hakira-ledger-* | 与模块名一致 |
| context-path | 不设 | 由 gateway 统一路由 |
