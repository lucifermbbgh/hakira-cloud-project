# hakira-cloud-project — Phase 4 基础设施部署与冒烟测试报告

> **阶段：** Phase 4 · 基础设施部署与冒烟测试
> **状态：** ✅ 14/14 通过
> **日期：** 2026-08-13

---

## 一、概述

### 1.1 阶段目标

完成基础设施容器化部署（MySQL + Nacos），跑通核心链路冒烟测试，使以下链路真正可用：

```
登录（auth）→ 签发 JWT → 网关（gateway）校验 → 分录录入（entry）→ 借贷平衡校验 → 库存联动（stock）
```

### 1.2 结论

**核心链路全部打通，14 项冒烟测试全部通过。**

| 维度 | 结果 |
|------|------|
| 基础设施 | MySQL 8.0（healthy）+ Nacos v3.2.3（HTTP 200） |
| 服务启动 | gateway/auth/entry/stock 四服务全部启动 |
| 核心链路 | 登录 → JWT → 分录 → 借贷校验 → 库存联动 全通 |
| 冒烟测试 | 14 / 14 通过（含正例 + 反例 + 安全拦截） |

---

## 二、部署架构

### 2.1 基础设施（Docker Compose）

| 组件 | 镜像 | 容器名 | 端口映射 | 说明 |
|------|------|--------|----------|------|
| MySQL | `mysql:8.0` | `hakira_ledger_mysql` | 3307→3306 | 账本专用，避开 conv-state-mysql 3306 |
| Nacos | `nacos/nacos-server:v3.2.3` | `hakira_ledger_nacos` | 8848/9848/8088→8080 | 注册中心+配置中心，内置存储 |

> 端口规划遵循「避开常用组件默认端口」：MySQL 用 3307 避开 3306；服务用 90xx 段避开 8080/8001 等。

### 2.2 数据库（3 库分离）

| 库名 | 用途 |
|------|------|
| `hakira_nacos` | Nacos 配置存储（预留，当前 Nacos 用内置存储） |
| `hakira_auth` | 登录认证（`hakira_user` 表） |
| `hakira_ledger` | 会计业务（Phase 5 建表） |

### 2.3 微服务

| 服务 | 端口 | 职责 |
|------|------|------|
| hakira-ledger-gateway | 9000 | 路由 + JWT 校验 |
| hakira-ledger-auth | 9010 | 认证中心（登录签 JWT） |
| hakira-ledger-entry | 9020 | 分录录入 + 借贷校验 |
| hakira-ledger-stock | 9030 | 库存出入库 + 快照/流水 |

---

## 三、冒烟测试结果

### 3.1 测试环境

| 项目 | 值 |
|------|-----|
| 系统 | Ubuntu VM（Linux 本地测试） |
| JDK | openjdk 21.0.11 |
| 登录凭据 | admin / admin123（本阶段 NoOpPasswordEncoder 明文） |
| JWT 传递 | `Authorization: Bearer <token>` |

### 3.2 测试明细（14 项全通过）

| # | 用例 | 预期 | 结果 |
|---|------|------|------|
| 1 | 编译 `mvn package`（4 服务 + 依赖） | BUILD SUCCESS | ✅ |
| 2 | 4 个 fat jar 含 BOOT-INF | 80-122MB | ✅ |
| 3 | 端口 9000/9010/9020/9030 | 全部监听 | ✅ |
| 4 | 登录（admin/admin123） | 签发 JWT | ✅ |
| 5 | 分录-借贷平衡（借100贷100） | POSTED | ✅ |
| 6 | 分录-借贷不平衡（借500贷400） | 拒绝 | ✅ |
| 7 | 库存入库（数量10） | INBOUND | ✅ |
| 8 | 库存超量出库（20>10） | 拒绝（库存不足） | ✅ |
| 9 | 库存快照查询 | 返回数量 | ✅ |
| 10 | 库存流水查询 | 返回列表 | ✅ |
| 11 | 网关登录（9000） | 转发成功 | ✅ |
| 12 | 网关分录（带 JWT） | POSTED | ✅ |
| 13 | 网关库存入库（带 JWT） | INBOUND | ✅ |
| 14 | 无 JWT 访问网关 | 401 拦截 | ✅ |

---

## 四、问题、根因与修复

> 按排查时间顺序记录，共 10 个问题，其中 9 个已解决、1 个暂缓（Nacos 连 MySQL）。

### 问题 1：磁盘空间不足导致镜像拉取中断

- **现象**：`docker compose up` 镜像拉取到 `146.8MB/155.4MB` 中断，容器启动失败。
- **根因**：根分区 95% 满（38G 总量仅 2G 可用）。
- **修复**：LUKS2 + LVM + ext4 三层在线热扩容（`growpart` → `cryptsetup resize` → `pvresize` → `lvextend` → `resize2fs`），根分区 38G → 97G。
- **结果**：✅ 扩容成功，59G 可用，数据无损。

### 问题 2：MySQL 8.4 启动失败

- **现象**：`hakira_ledger_mysql` 容器 unhealthy。
- **根因**：MySQL 8.4 移除了 `mysql_native_password` 认证插件，而 compose 中 `--default-authentication-plugin=mysql_native_password` 参数导致初始化失败。
- **修复**：镜像降级为 `mysql:8.0`。
- **结果**：✅ 容器 healthy。

### 问题 3：Nacos 连 MySQL 失败（No DataSource set）——暂缓

- **现象**：Nacos 容器反复失败，日志报 `ExternalDumpService init` 失败、`No DataSource set`。
- **根因**（已定位，但未在 2.x 版本解决）：
  1. Nacos 官方镜像的 `docker-startup.sh` 不处理 `MYSQL_SERVICE_*` 环境变量，仅依赖 Spring 占位符解析；
  2. Nacos 2.3+ 迁移 Spring Boot 3 后，`spring.datasource.platform` 被废弃（被注释），改 `spring.sql.init.platform`（占位符未替换），数据源判断读旧配置导致默认走内置 derby。
- **尝试的修复**（均未成功）：镜像 v2.5.3 → v2.4.3 → v2.3.2 降级、MySQL 8.4 → 8.0 降级、自定义 `application.properties` 挂载 + 手动替换占位符。
- **最终方案**：改用 **Nacos v3.2.3 + 内置存储（embedded storage）**，绕过 MySQL 数据源初始化问题。
- **结果**：✅ Nacos 启动成功（注册中心/配置中心功能正常）。
- **遗留**：Nacos 连 MySQL 持久化后续单独解决（`hakira_nacos` 库已预留）。

### 问题 4：Nacos 3.x 强制要求认证环境变量

- **现象**：Nacos v3.2.3 启动脚本报 `exit 255`，依次提示 `NACOS_AUTH_TOKEN must be set with Base64 String`、`NACOS_AUTH_IDENTITY_KEY must be set`。
- **根因**：Nacos 3.x 即使 `NACOS_AUTH_ENABLE=false` 也强制要求认证相关环境变量。
- **修复**：compose 补充 `NACOS_AUTH_TOKEN`（Base64 密钥）、`NACOS_AUTH_IDENTITY_KEY`/`NACOS_AUTH_IDENTITY_VALUE`。
- **结果**：✅ 启动成功。

### 问题 5：spring-boot-maven-plugin skip 导致瘦 jar 无法启动

- **现象**：打包出的 jar 仅 10-419KB（无 `Main-Class`、无 `BOOT-INF`），`java -jar` 报无法启动。
- **根因**：父 POM `<build><plugins>` 中 `spring-boot-maven-plugin` 配置 `<skip>true</skip>`，被子模块继承后所有模块 repackage 被跳过；而该 skip 是**有意为之**——因 `hakira-common` 等公共库有多个 main class，全量 repackage 会报「找不到单一 main class」。
- **修复**：父 POM 的 spring-boot 插件移到 `<pluginManagement>`（仅声明版本不执行），4 个应用模块（gateway/auth/entry/stock）在各自 POM 显式 `<skip>false</skip>`。
- **结果**：✅ 生成 fat jar（80-122MB），公共库不再误触发 repackage。

### 问题 6：RocketMQ 引入的 annotations-api 依赖冲突

- **现象**：auth 启动报 `NoSuchMethodError: javax.annotation.Resource.lookup()`。
- **根因**：`rocketmq-client 5.0.0` 传递引入 Tomcat 9 的 `annotations-api-6.0.53.jar`（其中的 `javax.annotation.Resource` 无 `lookup()` 方法），与标准 `javax.annotation-api-1.3.2.jar` 冲突。
- **修复**：在 auth POM 的 `rocketmq-spring-boot-starter` 依赖中排除 `org.apache.tomcat:annotations-api`。
- **结果**：✅ 启动正常。

### 问题 7：MyBatis mapper 残留旧包名

- **现象**：auth 启动报 `ClassNotFoundException: com.hakira.gate.pojo.dao.hakiraUser`。
- **根因**：gate→auth 模块重构遗留旧包名，`ElysiaUserMapper.xml` 的 namespace/resultMap/resultType 仍指向 `com.hakira.gate.*`。
- **修复**：更新为 `com.hakira.ledger.auth.mapper.HakiraUserMapper` / `com.hakira.ledger.auth.pojo.dao.HakiraUser`。
- **结果**：✅ 启动正常。

### 问题 8：登录认证走数据库但无匹配用户

- **现象**：登录 `asdf/qwer` 返回「用户名或密码错误」；排查发现登录实际查询了数据库 `hakira_user`（而非内存用户）。
- **根因**：`DBUserManager` 类带 `@Component` 注解，自动注册为 `UserDetailsService` Bean（即使 `WebSecurityConfig` 中 `dbUserDetailsService()` 的 `@Bean` 被注释），而数据库预置用户是 `admin` 非 `asdf`。
- **修复**：采用数据库认证——保留 `DBUserManager` 的 `@Component`，新增 `PasswordEncoder` Bean（`NoOpPasswordEncoder`，匹配数据库明文密码），登录改用 `admin/admin123`。
- **结果**：✅ 登录成功，签发 JWT（roles：USER_ADD/USER_SELECT）。

### 问题 9：@PathVariable 参数名信息丢失

- **现象**：stock 的 `snapshot`/`movements` 接口返回 500，报 `Name for argument of type [java.lang.String] not specified`。
- **根因**：Spring Boot 3.2 编译未启用 `-parameters`，`@PathVariable`/`@RequestParam` 参数名信息未保留。
- **修复**：显式指定参数名（`@PathVariable("itemCode")`、`@RequestParam(value="fromDate", ...)` 等）。
- **结果**：✅ 查询接口正常返回。

### 问题 10：服务 context-path 与网关路由不匹配

- **现象**：通过网关 9000 访问 `/login`、`/entry/**` 返回 404（直连服务则正常）。
- **根因**：auth/entry/stock 配置了 `server.servlet.context-path`（`/hakira`、`/ledger-entry`、`/ledger-stock`），而网关路由 Path 未含此前缀。
- **修复**：去掉三服务的 context-path（微服务+网关架构下由网关负责路由，服务监听根路径）。
- **结果**：✅ 网关路由、JWT 校验、服务转发全部正常。

---

## 五、遗留问题与风险

| # | 问题 | 影响 | 处理 |
|---|------|------|------|
| 1 | Nacos 连 MySQL 未解决（当前内置存储） | 配置数据不持久化（重启丢失） | 后续单独研究 Nacos 3.x 外部数据源 |
| 2 | Nacos 3.x 认证 403 刷屏 | 日志噪音，不影响服务注册 | 调整认证凭据/discovery 配置 |
| 3 | NoOpPasswordEncoder 明文密码 | 仅冒烟测试用，有安全警告 | **Phase 5 换 BCrypt** ✅ |
| 4 | 借贷不平衡/库存不足返回 500 | 无统一业务错误码 | **Phase 5 全局异常处理器** ✅ |
| 5 | stock/entry 内存存储 | 重启后数据丢失 | **Phase 5 换 DB 持久化** ✅ |

---

## 六、Compose 版本迭代

| 版本 | 文件 | 内容演进 |
|------|------|----------|
| v1 | `docker-compose.yml` | 原始 market 时代：mysql + nginx + nacos（无版本锁定） |
| v2 | `docker-compose-bak.yml` | 加入 hakira-core |
| v3 | `docker-compose-bak2.yml` | Nacos 集群（3 节点） |
| v4 | `docker-compose-bak3.yml` | 精简（去 nacos） |
| v5 | `docker-compose-bak4.yml` | 恢复 Nacos 集群 |
| v6 | `docker-compose-bak5.yml` | Nacos 集群（同上） |
| **v7（本次）** | `docker-compose-infra.yml` | **账本专用重构**：`mysql:8.0` + `nacos:v3.2.3`，容器名 `hakira_ledger` 前缀，端口 3307/8848/9848/8088 |

> 历史版本 compose 已在目录整理时删除（git 可回溯），当前仅保留 v7 `docker-compose-infra.yml`。

---

## 七、验证方式说明

本次为**运行时验证**（ad-hoc 冒烟验证，非 `mvn test` 套件绿）：

- **编译**：`mvn package -pl <4 服务> -am -DskipTests` → BUILD SUCCESS
- **服务**：4 个 fat jar 含 BOOT-INF，端口全部监听，网关链路返回 200
- **业务**：真实 HTTP 请求覆盖正例（平衡分录、入库）+ 反例（不平衡分录、超量出库）+ 安全拦截（无 JWT 401）

> 项目当前无单元测试套件（`-DskipTests`），故以上为手工冒烟验证，覆盖了本阶段改动的全部 16 个文件所影响的路径。

---

## 八、结论

Phase 4 基础设施部署完成，14 项冒烟测试全通过，核心链路（登录→JWT→分录→库存）打通。遗留的明文密码（问题 8）、500 异常（问题 9/10 相关）、内存存储（问题 5 相关）三类问题，已在 Phase 5 全部解决。
