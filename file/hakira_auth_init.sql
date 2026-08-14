-- ============================================================
-- hakira_auth 库：用户表 + 预置测试用户
-- 手动导入（MySQL 容器启动后执行）
-- ============================================================
USE hakira_auth;

-- 用户表（登录认证核心表）
CREATE TABLE IF NOT EXISTS `hakira_user` (
    `ID`                varchar(20)  NOT NULL COMMENT '用户ID',
    `USERNAME`          varchar(50)  NOT NULL COMMENT '用户名',
    `PASSWORD`          varchar(100) NOT NULL COMMENT '用户密码',
    `STATUS`            tinyint(4)   NULL DEFAULT NULL COMMENT '用户账号状态 1=正常 0=禁用',
    `ROLE_ID`           varchar(20)  NULL DEFAULT NULL COMMENT '用户角色ID',
    `REGISTRATION_DATE` varchar(10)  NULL DEFAULT NULL COMMENT '注册日期',
    `LAST_LOGIN_DATE`   varchar(10)  NULL DEFAULT NULL COMMENT '最后登录日期',
    `CREATE_USER`       varchar(20)  NULL DEFAULT NULL COMMENT '创建用户',
    `CREATE_DATE`       varchar(10)  NULL DEFAULT NULL COMMENT '创建日期',
    `CREATE_TIME`       varchar(8)   NULL DEFAULT NULL COMMENT '创建时间',
    `UPDATE_USER`       varchar(20)  NULL DEFAULT NULL COMMENT '更新用户',
    `UPDATE_DATE`       varchar(10)  NULL DEFAULT NULL COMMENT '更新日期',
    `UPDATE_TIME`       varchar(8)   NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- 预置测试用户（密码为 BCrypt 哈希，对应明文 admin123）
INSERT INTO `hakira_user` (`ID`, `USERNAME`, `PASSWORD`, `STATUS`, `REGISTRATION_DATE`)
VALUES ('1', 'admin', '$2a$10$73kpGgLjFUucG.Ua7V2N/.f5dIuFRTYNqC3vzD5bJmRunIKTaw/.K', 1, '2026-08-13');
