# hakira-cloud-project — Phase 4 基础设施部署与冒烟测试详细设计

> **阶段：** Phase 4 · 基础设施部署与冒烟测试
> **状态：** ✅ 已完成

---

## 一、基础设施配置（docker-compose-infra.yml）

```yaml
services:
  hakira_ledger_mysql:
    image: mysql:8.0
    ports: ["3307:3306"]
    environment:
      MYSQL_ROOT_PASSWORD: root
      TZ: Asia/Shanghai
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_general_ci
      - --default-authentication-plugin=mysql_native_password
    volumes:
      - hakira_ledger_mysql_data:/var/lib/mysql
      - ./mysql-init:/docker-entrypoint-initdb.d

  hakira_ledger_nacos:
    image: nacos/nacos-server:v3.2.3
    ports: ["8848:8848", "9848:9848", "8088:8080"]
    environment:
      MODE: standalone
      NACOS_AUTH_ENABLE: "false"
      NACOS_AUTH_TOKEN: "<Base64密钥>"       # 3.x 即使关认证也强制要求
      NACOS_AUTH_IDENTITY_KEY: "serverIdentity"
      NACOS_AUTH_IDENTITY_VALUE: "security"
```

## 二、数据库初始化

### 2.1 建库（mysql-init/init.sql）

```sql
CREATE DATABASE IF NOT EXISTS hakira_nacos ...;
CREATE DATABASE IF NOT EXISTS hakira_auth ...;
CREATE DATABASE IF NOT EXISTS hakira_ledger ...;
```

### 2.2 认证用户表（hakira_auth_init.sql）

```sql
CREATE TABLE hakira_user (
    ID BIGINT PRIMARY KEY,
    USERNAME VARCHAR(255),
    PASSWORD VARCHAR(255),
    STATUS INT,
    REGISTRATION_DATE DATE
);
-- 预置 admin 用户
```

## 三、服务配置设计

### 3.1 端口与命名

| 服务 | 端口 | spring.application.name | 注册名 |
|------|------|------------------------|--------|
| gateway | 9000 | hakira-ledger-gateway | 同左 |
| auth | 9010 | hakira-ledger-auth | 同左 |
| entry | 9020 | hakira-ledger-entry | 同左 |
| stock | 9030 | hakira-ledger-stock | 同左 |

### 3.2 关键配置调整

| 配置项 | 变更 | 原因 |
|--------|------|------|
| context-path | 去掉 | 网关路由不匹配导致 404 |
| @PathVariable | 显式参数名 | Boot 3.2 编译未加 -parameters |
| bootstrap | register-enabled=true | 服务注册到 Nacos |

## 四、构建配置设计

### 4.1 spring-boot-maven-plugin

父 POM 移到 `<pluginManagement>`（仅声明版本不执行），4 个应用模块各自 POM 显式 `<skip>false</skip>` + mainClass。

| 模块 | mainClass |
|------|-----------|
| gateway | HakiraLedgerGatewayApplication |
| auth | HakiraLedgerAuthApplication |
| entry | HakiraLedgerEntryApplication |
| stock | HakiraLedgerStockApplication |

### 4.2 依赖冲突处理

| 依赖 | 处理 |
|------|------|
| rocketmq annotations-api | 排除 org.apache.tomcat:annotations-api |

## 五、认证链路设计

```
POST /login → DBUserManager（@Component 查 hakira_user）
    → PasswordEncoder 匹配（本阶段 NoOpPasswordEncoder）
    → 签发 JWT → gateway 校验 → 放行/401
```
