-- ============================================================
-- hakira-cloud-project Phase 14：存货核算与计价
-- 1. stock_snapshot 加成本字段（总成本 + 加权平均单价）
-- 2. stock_movement 加成本字段（单价 + 本次总成本）
-- 幂等：可重复执行（IF NOT EXISTS 逻辑由 ADD COLUMN 报错提示，重跑前需确认）
-- ============================================================

USE hakira_ledger;

ALTER TABLE stock_snapshot
    ADD COLUMN total_cost        DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '库存总成本' AFTER current_quantity,
    ADD COLUMN weighted_avg_cost DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '加权平均单价' AFTER total_cost;

ALTER TABLE stock_movement
    ADD COLUMN unit_cost  DECIMAL(18,4) NULL COMMENT '单价' AFTER quantity,
    ADD COLUMN total_cost DECIMAL(18,2) NULL COMMENT '本次总成本' AFTER unit_cost;

-- direction 原 varchar(8) 不足以容纳 STOCKTAKE_GAIN/LOSS，加长
ALTER TABLE stock_movement
    MODIFY COLUMN direction VARCHAR(20) NOT NULL COMMENT '方向：INBOUND/OUTBOUND/STOCKTAKE_GAIN/STOCKTAKE_LOSS';
