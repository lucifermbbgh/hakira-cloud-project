-- ============================================================
-- hakira-cloud-project Phase 12：固定资产管理
-- 1. fixed_asset：固定资产卡片表
-- 幂等：可重复执行
-- ============================================================

USE hakira_ledger;

CREATE TABLE IF NOT EXISTS fixed_asset (
    asset_code              VARCHAR(32)   NOT NULL COMMENT '资产编码',
    asset_name              VARCHAR(100)  NOT NULL COMMENT '资产名称',
    category                VARCHAR(32)   NULL COMMENT '资产类别',
    original_value          DECIMAL(18,2) NOT NULL COMMENT '原值',
    residual_rate           DECIMAL(5,4)  NOT NULL DEFAULT 0.0500 COMMENT '残值率',
    useful_life             INT           NOT NULL COMMENT '折旧年限（月）',
    depreciation_method     VARCHAR(32)   NOT NULL DEFAULT 'STRAIGHT_LINE' COMMENT '折旧方法：STRAIGHT_LINE/DOUBLE_DECLINING',
    accumulated_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计折旧',
    net_value               DECIMAL(18,2) NOT NULL COMMENT '净值',
    status                  VARCHAR(16)   NOT NULL DEFAULT 'IN_USE' COMMENT '状态：IN_USE/DISPOSED',
    purchase_date           DATE          NULL COMMENT '购置日期',
    version                 INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (asset_code)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '固定资产卡片表';
