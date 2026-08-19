-- ============================================================
-- hakira-cloud-project Phase 15：预算管理
-- 1. budget：预算表（期间 + 科目 + 预算金额）
-- 幂等：可重复执行
-- ============================================================

USE hakira_ledger;

CREATE TABLE IF NOT EXISTS budget (
    period        VARCHAR(6)    NOT NULL COMMENT '会计期间 YYYYMM',
    subject_code  VARCHAR(20)   NOT NULL COMMENT '科目编码',
    budget_amount DECIMAL(18,2) NOT NULL COMMENT '预算金额',
    version       INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (period, subject_code)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '预算表（期间 + 科目 + 预算金额）';
