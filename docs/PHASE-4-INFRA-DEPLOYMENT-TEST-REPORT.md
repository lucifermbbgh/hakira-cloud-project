# hakira-cloud-project — Phase 4 基础设施部署与冒烟测试报告

> **阶段：** Phase 4 · 基础设施部署与冒烟测试
> **状态：** ✅ 14/14 通过
> **详细版：** `2026-08-13-基础设施部署与冒烟测试报告.md`

---

## 一、测试范围

核心链路冒烟测试：登录 → JWT → 分录 → 借贷校验 → 库存联动，含正例 + 反例 + 安全拦截。

## 二、测试环境

| 项目 | 值 |
|------|-----|
| 系统 | Ubuntu VM（Linux 本地测试） |
| JDK | openjdk 21.0.11 |
| 基础设施 | MySQL 8.0 + Nacos v3.2.3 |
| 登录凭据 | admin / admin123（本阶段 NoOpPasswordEncoder 明文） |

## 三、测试用例与结果（14 项全通过）

| # | 用例 | 预期 | 结果 |
|---|------|------|------|
| 1 | 编译 4 服务 + 依赖 | BUILD SUCCESS | ✅ |
| 2 | 4 个 fat jar 含 BOOT-INF | 80-122MB | ✅ |
| 3 | 端口 9000/9010/9020/9030 | 全部监听 | ✅ |
| 4 | 登录 admin/admin123 | 签发 JWT | ✅ |
| 5 | 分录借贷平衡（借100贷100） | POSTED | ✅ |
| 6 | 分录借贷不平衡（借500贷400） | 拒绝 | ✅ |
| 7 | 库存入库（10） | INBOUND | ✅ |
| 8 | 库存超量出库（20>10） | 拒绝 | ✅ |
| 9 | 库存快照查询 | 返回数量 | ✅ |
| 10 | 库存流水查询 | 返回列表 | ✅ |
| 11 | 网关登录（9000） | 转发成功 | ✅ |
| 12 | 网关分录（带 JWT） | POSTED | ✅ |
| 13 | 网关库存入库（带 JWT） | INBOUND | ✅ |
| 14 | 无 JWT 访问网关 | 401 拦截 | ✅ |

## 四、问题、根因与处理（10 个）

| # | 问题 | 根因 | 处理方案 | 结果 |
|---|------|------|---------|------|
| 1 | 磁盘不足镜像拉取中断 | 根分区 95% 满 | LUKS2+LVM+ext4 在线扩容 38G→97G | ✅ |
| 2 | MySQL 8.4 启动失败 | 8.4 移除 mysql_native_password | 降级 mysql:8.0 | ✅ |
| 3 | Nacos 连 MySQL 失败 | 官方镜像无 MySQL env 处理 + Boot3 配置废弃 | 改用内置 derby 存储 | ✅（暂缓连 MySQL） |
| 4 | Nacos 3.x 认证变量强制 | 关认证仍要求 NACOS_AUTH_TOKEN | 补认证环境变量 | ✅ |
| 5 | 瘦 jar 无法启动 | 父 POM spring-boot skip 继承 | 插件移 pluginManagement + 显式 skip=false | ✅ |
| 6 | RocketMQ annotations-api 冲突 | rocketmq 传递旧 Tomcat 注解 API | 排除 annotations-api | ✅ |
| 7 | mapper 残留旧包名 | gate→auth 重构遗留 | 更新 com.hakira.ledger.auth.* | ✅ |
| 8 | 登录无匹配用户 | DBUserManager @Component 自动注册 | 数据库认证 + NoOpPasswordEncoder | ✅ |
| 9 | @PathVariable 参数名丢失 | Boot 3.2 编译未加 -parameters | 显式指定参数名 | ✅ |
| 10 | context-path 与网关不匹配 | 服务设了 context-path | 去掉，由网关统一路由 | ✅ |

## 五、遗留问题（Phase 5 解决）

| 问题 | Phase 5 处理 |
|------|-------------|
| NoOpPasswordEncoder 明文密码 | 换 BCrypt 哈希 |
| 借贷不平衡/库存不足返回 500 | 全局异常处理器 + 业务错误码 |
| entry/stock 内存存储 | 换 MySQL 分区表持久化 |

## 六、Compose 版本迭代

| 版本 | 文件 | 演进 |
|------|------|------|
| v1-v6 | docker-compose.yml + bak 系列 | market 时代：mysql+nginx+nacos |
| **v7** | docker-compose-infra.yml | 账本专用：mysql:8.0 + nacos:v3.2.3 |

## 七、结论

Phase 4 基础设施部署完成，14 项冒烟测试全通过，核心链路（登录→JWT→分录→库存）打通。遗留的安全（明文密码）、异常处理（500）、持久化（内存存储）问题在 Phase 5 解决。
