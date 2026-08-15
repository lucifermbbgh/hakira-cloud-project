-- ============================================================
-- hakira-cloud-project Phase 16：审计合规与系统治理
-- 1. audit_log：操作审计日志表
-- 幂等：可重复执行
-- ============================================================

USE hakira_ledger;

CREATE TABLE IF NOT EXISTS audit_log (
    log_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    operation   VARCHAR(64)  NOT NULL COMMENT '操作类型',
    operator    VARCHAR(64)  NULL COMMENT '操作人',
    entity_type VARCHAR(32)  NULL COMMENT '实体类型',
    entity_id   VARCHAR(64)  NULL COMMENT '实体ID',
    detail      VARCHAR(500) NULL COMMENT '详情',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_entity (entity_type, entity_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '操作审计日志表';
