-- ============================================================
-- hakira-cloud-project Phase 7：会计科目与辅助核算体系
-- 1. 扩展 account_subject（加 parent_code + subject_level 层级）
-- 2. 预置完整《企业会计准则》一级科目（21 → 79）
-- 3. 新建辅助核算 3 张表（维度字典 / 维度值 / 分录行关联）
-- 4. 预置辅助核算维度 + 示例维度值
-- 幂等：可重复执行（INSERT ... ON DUPLICATE KEY UPDATE）
-- ============================================================

USE hakira_ledger;

-- ==================== 1. 扩展科目表（层级） ====================
ALTER TABLE account_subject
    ADD COLUMN parent_code   VARCHAR(20)  NULL     COMMENT '上级科目编码（NULL=一级）' AFTER balance_direction,
    ADD COLUMN subject_level TINYINT      NOT NULL DEFAULT 1 COMMENT '科目级次（1一级/2二级…）' AFTER parent_code;

-- ==================== 2. 预置完整一级科目（79 个） ====================
INSERT INTO account_subject (subject_code, subject_name, category, balance_direction, parent_code, subject_level) VALUES
-- 资产类（1xxx）
('1001','库存现金','资产','D',NULL,1),
('1002','银行存款','资产','D',NULL,1),
('1012','其他货币资金','资产','D',NULL,1),
('1101','交易性金融资产','资产','D',NULL,1),
('1121','应收票据','资产','D',NULL,1),
('1122','应收账款','资产','D',NULL,1),
('1123','预付账款','资产','D',NULL,1),
('1131','应收股利','资产','D',NULL,1),
('1132','应收利息','资产','D',NULL,1),
('1221','其他应收款','资产','D',NULL,1),
('1231','坏账准备','资产','C',NULL,1),
('1401','材料采购','资产','D',NULL,1),
('1402','在途物资','资产','D',NULL,1),
('1403','原材料','资产','D',NULL,1),
('1404','材料成本差异','资产','D',NULL,1),
('1405','库存商品','资产','D',NULL,1),
('1406','发出商品','资产','D',NULL,1),
('1408','委托加工物资','资产','D',NULL,1),
('1411','周转材料','资产','D',NULL,1),
('1471','存货跌价准备','资产','C',NULL,1),
('1511','长期股权投资','资产','D',NULL,1),
('1512','长期股权投资减值准备','资产','C',NULL,1),
('1521','投资性房地产','资产','D',NULL,1),
('1531','长期应收款','资产','D',NULL,1),
('1601','固定资产','资产','D',NULL,1),
('1602','累计折旧','资产','C',NULL,1),
('1603','固定资产减值准备','资产','C',NULL,1),
('1604','在建工程','资产','D',NULL,1),
('1605','工程物资','资产','D',NULL,1),
('1606','固定资产清理','资产','D',NULL,1),
('1701','无形资产','资产','D',NULL,1),
('1702','累计摊销','资产','C',NULL,1),
('1703','无形资产减值准备','资产','C',NULL,1),
('1711','商誉','资产','D',NULL,1),
('1801','长期待摊费用','资产','D',NULL,1),
('1811','递延所得税资产','资产','D',NULL,1),
('1901','待处理财产损溢','资产','D',NULL,1),
-- 负债类（2xxx）
('2001','短期借款','负债','C',NULL,1),
('2101','交易性金融负债','负债','C',NULL,1),
('2201','应付票据','负债','C',NULL,1),
('2202','应付账款','负债','C',NULL,1),
('2203','预收账款','负债','C',NULL,1),
('2211','应付职工薪酬','负债','C',NULL,1),
('2221','应交税费','负债','C',NULL,1),
('2231','应付利息','负债','C',NULL,1),
('2232','应付股利','负债','C',NULL,1),
('2241','其他应付款','负债','C',NULL,1),
('2401','递延收益','负债','C',NULL,1),
('2501','长期借款','负债','C',NULL,1),
('2502','应付债券','负债','C',NULL,1),
('2701','长期应付款','负债','C',NULL,1),
('2801','预计负债','负债','C',NULL,1),
('2901','递延所得税负债','负债','C',NULL,1),
-- 权益类（4xxx）
('4001','实收资本','权益','C',NULL,1),
('4002','资本公积','权益','C',NULL,1),
('4101','盈余公积','权益','C',NULL,1),
('4103','本年利润','权益','C',NULL,1),
('4104','利润分配','权益','C',NULL,1),
('4201','库存股','权益','D',NULL,1),
-- 成本类（5xxx）
('5001','生产成本','成本','D',NULL,1),
('5101','制造费用','成本','D',NULL,1),
('5201','劳务成本','成本','D',NULL,1),
('5301','研发支出','成本','D',NULL,1),
-- 损益类（6xxx）
('6001','主营业务收入','损益','C',NULL,1),
('6051','其他业务收入','损益','C',NULL,1),
('6101','公允价值变动损益','损益','C',NULL,1),
('6111','投资收益','损益','C',NULL,1),
('6301','营业外收入','损益','C',NULL,1),
('6401','主营业务成本','损益','D',NULL,1),
('6402','其他业务成本','损益','D',NULL,1),
('6403','税金及附加','损益','D',NULL,1),
('6601','销售费用','损益','D',NULL,1),
('6602','管理费用','损益','D',NULL,1),
('6603','财务费用','损益','D',NULL,1),
('6701','资产减值损失','损益','D',NULL,1),
('6711','营业外支出','损益','D',NULL,1),
('6801','所得税费用','损益','D',NULL,1),
('6901','以前年度损益调整','损益','D',NULL,1)
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), category = VALUES(category), balance_direction = VALUES(balance_direction);

-- ==================== 3. 预置示例明细科目（二级） ====================
INSERT INTO account_subject (subject_code, subject_name, category, balance_direction, parent_code, subject_level) VALUES
('100201','银行存款-工商银行','资产','D','1002',2),
('100202','银行存款-建设银行','资产','D','1002',2),
('222101','应交税费-应交增值税','负债','C','2221',2),
('222102','应交税费-应交所得税','负债','C','2221',2)
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name);

-- ==================== 4. 辅助核算维度字典 ====================
CREATE TABLE IF NOT EXISTS auxiliary_dimension (
    dimension_code VARCHAR(20)  NOT NULL COMMENT '维度编码',
    dimension_name VARCHAR(50)  NOT NULL COMMENT '维度名称',
    status         VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dimension_code)
) COMMENT='辅助核算维度字典';

-- ==================== 5. 辅助核算维度值 ====================
CREATE TABLE IF NOT EXISTS auxiliary_value (
    value_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '维度值ID',
    dimension_code VARCHAR(20)  NOT NULL COMMENT '所属维度编码',
    value_code     VARCHAR(50)  NOT NULL COMMENT '维度值编码',
    value_name     VARCHAR(100) NOT NULL COMMENT '维度值名称',
    status         VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (value_id),
    UNIQUE KEY uk_dim_value (dimension_code, value_code)
) COMMENT='辅助核算维度值';

-- ==================== 6. 分录行辅助核算关联 ====================
CREATE TABLE IF NOT EXISTS journal_entry_line_aux (
    line_id        BIGINT      NOT NULL COMMENT '分录行ID',
    dimension_code VARCHAR(20) NOT NULL COMMENT '维度编码',
    value_code     VARCHAR(50) NOT NULL COMMENT '维度值编码',
    PRIMARY KEY (line_id, dimension_code)
) COMMENT='分录行辅助核算关联（一行可挂多维度）';

-- ==================== 7. 预置辅助核算维度 + 示例值 ====================
INSERT INTO auxiliary_dimension (dimension_code, dimension_name) VALUES
('DEPT','部门'), ('PROJECT','项目'), ('CUSTOMER','客户'), ('SUPPLIER','供应商'), ('CASH_FLOW','现金流量')
ON DUPLICATE KEY UPDATE dimension_name = VALUES(dimension_name);

INSERT INTO auxiliary_value (dimension_code, value_code, value_name) VALUES
('DEPT','D001','技术部'), ('DEPT','D002','财务部'), ('DEPT','D003','销售部'),
('PROJECT','P001','研发项目A'), ('PROJECT','P002','工程项目B'),
('CUSTOMER','C001','客户甲'), ('CUSTOMER','C002','客户乙'),
('SUPPLIER','S001','供应商丙'), ('SUPPLIER','S002','供应商丁'),
('CASH_FLOW','CF001','经营活动'), ('CASH_FLOW','CF002','投资活动'), ('CASH_FLOW','CF003','筹资活动')
ON DUPLICATE KEY UPDATE value_name = VALUES(value_name);
