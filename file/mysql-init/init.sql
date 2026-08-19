-- ============================================================
-- hakira-cloud-project 数据库初始化
-- 在 MySQL 容器首次启动时自动执行
-- ============================================================

-- Nacos 配置存储库（Nacos 启动时自动建表）
CREATE DATABASE IF NOT EXISTS `hakira_nacos`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 登录认证库
CREATE DATABASE IF NOT EXISTS `hakira_auth`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 会计业务库
CREATE DATABASE IF NOT EXISTS `hakira_ledger`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
