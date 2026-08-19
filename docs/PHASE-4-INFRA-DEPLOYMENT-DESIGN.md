# hakira-cloud-project — Phase 4 基础设施部署与冒烟测试方案设计

> **阶段：** Phase 4 · 基础设施部署与冒烟测试
> **状态：** ✅ 已完成
> **关联提交：** d21faa2d · dd489e32 · 4c93ba39 · a793a8ad · efb28519 · ac7cf0bb · 6c670d87

---

## 一、阶段目标

完成基础设施容器化部署（MySQL + Nacos），并跑通核心链路冒烟测试，使「登录 → JWT → 分录 → 借贷校验 → 库存联动」链路真正可用。

## 二、部署架构设计

### 2.1 基础设施

| 组件 | 镜像 | 容器名 | 端口 | 说明 |
|------|------|--------|------|------|
| MySQL | `mysql:8.0` | hakira_ledger_mysql | 3307→3306 | 账本专用，避开 3306 |
| Nacos | `nacos/nacos-server:v3.2.3` | hakira_ledger_nacos | 8848/9848/8088→8080 | 注册/配置中心，内置存储 |

### 2.2 应用服务

| 服务 | 端口 | 职责 |
|------|------|------|
| gateway | 9000 | 路由 + JWT 校验 |
| auth | 9010 | 认证中心 |
| entry | 9020 | 分录录入 + 借贷校验 |
| stock | 9030 | 库存出入库 + 快照 |

### 2.3 数据库设计（3 库分离）

| 库名 | 用途 |
|------|------|
| hakira_nacos | Nacos 配置存储（预留） |
| hakira_auth | 登录认证（hakira_user 表） |
| hakira_ledger | 会计业务（Phase 5 建表） |

## 三、关键设计决策

| 决策 | 方案 | 理由 |
|------|------|------|
| MySQL 版本 | 8.0（非 8.4） | 8.4 移除 mysql_native_password |
| Nacos 存储 | 内置 derby（非 MySQL） | 连 MySQL 有已知坑，先跑通主链路 |
| 端口规划 | 避开常用默认端口 | 3307 避 3306，90xx 避 8080 |
| 服务 context-path | 不设 | 由网关统一路由 |
| 构建 | spring-boot 插件移到 pluginManagement | 避免公共库误 repackage |

## 四、阶段验收标准

- [x] MySQL + Nacos 容器 healthy/启动成功
- [x] 4 服务 fat jar 可启动
- [x] 14 项冒烟测试通过（正例 + 反例 + 安全拦截）
