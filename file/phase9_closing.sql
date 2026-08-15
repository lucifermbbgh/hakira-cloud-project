-- ============================================================
-- hakira-cloud-project Phase 9：期末结账与账务结转
-- 1. accounting_period：会计期间结账状态（OPEN/CLOSING/CLOSED）
-- 2. account_balance：科目余额物化表（期初/本期发生/期末）
-- 幂等：可重复执行
-- ============================================================

USE hakira_ledger;

-- ==================== 1. 会计期间状态表 ====================
CREATE TABLE IF NOT EXISTS accounting_period (
    period      VARCHAR(6)  NOT NULL COMMENT '会计期间 YYYYMM',
    status      VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN=未结账 CLOSING=结账中 CLOSED=已结账',
    closed_by   VARCHAR(64) NULL COMMENT '结账人',
    closed_at   DATETIME    NULL COMMENT '结账时间',
    version     INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (period)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会计期间结账状态表';

-- ==================== 2. 科目余额物化表 ====================
-- 结账时物化：期初余额（承接上期）+ 本期发生额（聚合流水）+ 期末余额
CREATE TABLE IF NOT EXISTS account_balance (
    period         VARCHAR(6)    NOT NULL COMMENT '会计期间 YYYYMM',
    subject_code   VARCHAR(20)   NOT NULL COMMENT '科目编码',
    subject_name   VARCHAR(100)  NULL COMMENT '科目名称',
    opening_debit  DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '期初借方余额',
    opening_credit DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '期初贷方余额',
    period_debit   DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本期借方发生额',
    period_credit  DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本期贷方发生额',
    closing_debit  DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '期末借方余额',
    closing_credit DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '期末贷方余额',
    version        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (period, subject_code)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '科目余额物化表（期初/本期发生/期末）';
