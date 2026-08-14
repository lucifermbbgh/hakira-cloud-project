-- ============================================================
-- hakira_ledger 库：会计业务表结构
-- 设计要点：
--   1. 分录头表/分录行表/库存流水表 按月分区（RANGE COLUMNS by 日期）
--   2. 库存快照表 为「状态表」（每物资一行），不分
--   3. 会计科目表 为「字典表」（国家标准科目），不分
--   4. 所有业务表含 status(状态) + version(乐观锁) + create_time/update_time(审计)
-- ============================================================

USE hakira_ledger;

-- ============================================================
-- 1. 会计科目表（字典表，国家标准会计科目）
--    分录的 subject_code 必须在本表存在（业务层校验 + 应用层外键）
-- ============================================================
CREATE TABLE IF NOT EXISTS `account_subject` (
    `subject_code`     varchar(20)  NOT NULL COMMENT '科目编码（国家标准，如 1001/2202）',
    `subject_name`     varchar(100) NOT NULL COMMENT '科目名称',
    `category`         varchar(20)  NOT NULL COMMENT '科目类别：资产/负债/共同/权益/成本/损益',
    `balance_direction` char(1)      NOT NULL COMMENT '余额方向：D=借 C=贷',
    `status`           varchar(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE=启用 DISABLED=停用',
    `version`          int          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`subject_code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会计科目表（国家标准会计科目）';

-- ============================================================
-- 2. 分录头表（按月分区，分区键 entry_date）
--    主键 (entry_id, entry_date)：分区键必须在主键内
-- ============================================================
CREATE TABLE IF NOT EXISTS `journal_entry` (
    `entry_id`     varchar(20)    NOT NULL COMMENT '分录ID（20位雪花ID）',
    `entry_date`   date           NOT NULL COMMENT '记账日期（分区键）',
    `voucher_no`   varchar(64)    NULL DEFAULT NULL COMMENT '凭证号',
    `description`  varchar(255)   NULL DEFAULT NULL COMMENT '摘要',
    `total_debit`  decimal(18,2)  NOT NULL DEFAULT 0.00 COMMENT '借方合计',
    `total_credit` decimal(18,2)  NOT NULL DEFAULT 0.00 COMMENT '贷方合计',
    `status`       varchar(16)    NOT NULL DEFAULT 'POSTED' COMMENT '状态：POSTED=已入账 REVERSED=已冲销',
    `version`      int            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`entry_id`, `entry_date`) USING BTREE,
    KEY `idx_voucher` (`voucher_no`) USING BTREE,
    KEY `idx_status_date` (`status`, `entry_date`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会计分录头表（按月分区）'
  PARTITION BY RANGE COLUMNS (`entry_date`) (
      PARTITION p202607 VALUES LESS THAN ('2026-08-01'),
      PARTITION p202608 VALUES LESS THAN ('2026-09-01'),
      PARTITION p202609 VALUES LESS THAN ('2026-10-01'),
      PARTITION p202610 VALUES LESS THAN ('2026-11-01'),
      PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
      PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
      PARTITION pmax VALUES LESS THAN (MAXVALUE)
  );

-- ============================================================
-- 3. 分录行表（按月分区，分区键 entry_date 冗余自主表）
--    主键 (line_id, entry_date)
-- ============================================================
CREATE TABLE IF NOT EXISTS `journal_entry_line` (
    `line_id`        bigint        NOT NULL AUTO_INCREMENT COMMENT '行ID（自增）',
    `entry_id`       varchar(20)   NOT NULL COMMENT '分录ID',
    `entry_date`     date          NOT NULL COMMENT '记账日期（分区键，冗余自主表）',
    `line_no`        int           NOT NULL COMMENT '行号',
    `subject_code`   varchar(20)   NOT NULL COMMENT '科目编码（引用 account_subject）',
    `subject_name`   varchar(100)  NULL DEFAULT NULL COMMENT '科目名称（快照冗余）',
    `description`    varchar(255)  NULL DEFAULT NULL COMMENT '摘要',
    `debit_amount`   decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '借方金额',
    `credit_amount`  decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '贷方金额',
    `create_time`    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`line_id`, `entry_date`) USING BTREE,
    KEY `idx_entry` (`entry_id`, `entry_date`) USING BTREE,
    KEY `idx_subject` (`subject_code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '会计分录行表（按月分区）'
  PARTITION BY RANGE COLUMNS (`entry_date`) (
      PARTITION p202607 VALUES LESS THAN ('2026-08-01'),
      PARTITION p202608 VALUES LESS THAN ('2026-09-01'),
      PARTITION p202609 VALUES LESS THAN ('2026-10-01'),
      PARTITION p202610 VALUES LESS THAN ('2026-11-01'),
      PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
      PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
      PARTITION pmax VALUES LESS THAN (MAXVALUE)
  );

-- ============================================================
-- 4. 库存流水表（按月分区，分区键 movement_date）
--    事件表/明细表：append-only，记录每次入出库事件
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_movement` (
    `movement_id`         varchar(20)    NOT NULL COMMENT '流水ID（20位雪花ID）',
    `item_code`           varchar(64)    NOT NULL COMMENT '物资编码',
    `item_name`           varchar(100)   NULL DEFAULT NULL COMMENT '物资名称',
    `direction`           varchar(8)     NOT NULL COMMENT '方向：INBOUND=入库 OUTBOUND=出库',
    `quantity`            decimal(18,4)  NOT NULL COMMENT '变动数量',
    `unit`                varchar(16)    NULL DEFAULT NULL COMMENT '单位',
    `related_voucher_no`  varchar(64)    NULL DEFAULT NULL COMMENT '关联凭证号',
    `movement_date`       date           NOT NULL COMMENT '变动日期（分区键）',
    `remark`              varchar(255)   NULL DEFAULT NULL COMMENT '备注',
    `status`              varchar(16)    NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE=有效 REVERSED=已冲销',
    `version`             int            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`movement_id`, `movement_date`) USING BTREE,
    KEY `idx_item_date` (`item_code`, `movement_date`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '库存流水表（按月分区）'
  PARTITION BY RANGE COLUMNS (`movement_date`) (
      PARTITION p202607 VALUES LESS THAN ('2026-08-01'),
      PARTITION p202608 VALUES LESS THAN ('2026-09-01'),
      PARTITION p202609 VALUES LESS THAN ('2026-10-01'),
      PARTITION p202610 VALUES LESS THAN ('2026-11-01'),
      PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
      PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
      PARTITION pmax VALUES LESS THAN (MAXVALUE)
  );

-- ============================================================
-- 5. 库存快照表（状态表/物化汇总表，每物资一行，不分）
--    作用：缓存每个物资的「当前库存量」，避免每次查询 SUM 流水
--    入/出库时事务内 upsert，出库前据此校验库存
-- ============================================================
CREATE TABLE IF NOT EXISTS `stock_snapshot` (
    `item_code`        varchar(64)    NOT NULL COMMENT '物资编码',
    `item_name`        varchar(100)   NULL DEFAULT NULL COMMENT '物资名称',
    `current_quantity` decimal(18,4)  NOT NULL DEFAULT 0.0000 COMMENT '当前库存量',
    `unit`             varchar(16)    NULL DEFAULT NULL COMMENT '单位',
    `status`           varchar(16)    NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE=有效',
    `version`          int            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`item_code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '库存快照表（状态表，每物资一行）';

-- ============================================================
-- 预置数据：国家标准会计科目（核心科目集）
-- ============================================================
INSERT INTO `account_subject` (`subject_code`, `subject_name`, `category`, `balance_direction`) VALUES
    -- 资产类（借）
    ('1001', '库存现金',     '资产', 'D'),
    ('1002', '银行存款',     '资产', 'D'),
    ('1122', '应收账款',     '资产', 'D'),
    ('1403', '原材料',       '资产', 'D'),
    ('1405', '库存商品',     '资产', 'D'),
    ('1601', '固定资产',     '资产', 'D'),
    -- 负债类（贷）
    ('2001', '短期借款',     '负债', 'C'),
    ('2202', '应付账款',     '负债', 'C'),
    ('2211', '应付职工薪酬', '负债', 'C'),
    -- 所有者权益类（贷）
    ('4001', '实收资本',     '权益', 'C'),
    ('4103', '本年利润',     '权益', 'C'),
    ('4104', '利润分配',     '权益', 'C'),
    -- 成本类（借）
    ('5001', '生产成本',     '成本', 'D'),
    ('5101', '制造费用',     '成本', 'D'),
    -- 损益类（收入贷/费用借）
    ('6001', '主营业务收入', '损益', 'C'),
    ('6051', '其他业务收入', '损益', 'C'),
    ('6401', '主营业务成本', '损益', 'D'),
    ('6403', '税金及附加',   '损益', 'D'),
    ('6601', '销售费用',     '损益', 'D'),
    ('6602', '管理费用',     '损益', 'D'),
    ('6603', '财务费用',     '损益', 'D');
