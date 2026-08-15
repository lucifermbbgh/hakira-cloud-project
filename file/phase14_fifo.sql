-- ============================================================
-- hakira-cloud-project Phase 14 延伸：FIFO 计价
-- 1. inventory_lot：库存批次表（FIFO 用）
-- 2. stock_snapshot 加 costing_method 计价方法字段
-- ============================================================

USE hakira_ledger;

CREATE TABLE IF NOT EXISTS inventory_lot (
    lot_id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '批次ID',
    item_code          VARCHAR(64)   NOT NULL COMMENT '物资编码',
    unit_cost          DECIMAL(18,4) NOT NULL COMMENT '批次单价',
    remaining_quantity DECIMAL(18,4) NOT NULL COMMENT '剩余数量',
    inbound_date       DATE          NOT NULL COMMENT '入库日期',
    status             VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/EXHAUSTED',
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (lot_id),
    KEY idx_item (item_code, inbound_date)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '库存批次表（FIFO 计价）';

ALTER TABLE stock_snapshot
    ADD COLUMN costing_method VARCHAR(32) NOT NULL DEFAULT 'WEIGHTED_AVG' COMMENT '计价方法：WEIGHTED_AVG/FIFO' AFTER weighted_avg_cost;
