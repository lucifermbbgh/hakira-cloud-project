-- ============================================================
-- hakira-cloud-project Phase 11：成本核算
-- 预置成本项目（COST_ITEM）辅助核算维度 + 料工费三个项目值
-- 幂等：可重复执行
-- ============================================================

USE hakira_ledger;

INSERT INTO auxiliary_dimension (dimension_code, dimension_name) VALUES
('COST_ITEM','成本项目')
ON DUPLICATE KEY UPDATE dimension_name = VALUES(dimension_name);

INSERT INTO auxiliary_value (dimension_code, value_code, value_name) VALUES
('COST_ITEM','DM','直接材料'),
('COST_ITEM','DL','直接人工'),
('COST_ITEM','MO','制造费用')
ON DUPLICATE KEY UPDATE value_name = VALUES(value_name);
