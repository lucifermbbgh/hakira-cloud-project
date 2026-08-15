# PHASE-10 财务报表体系（三大报表）— 详细设计

> **版本：** v1.0 · 2026-08-15
> **前置：** PHASE-10-FINANCIAL-STATEMENTS-DESIGN.md

---

## 一、数据来源（复用现有表，不新增）

| 报表 | 查询 | 数据源 |
|------|------|--------|
| 资产负债表 | selectBalanceByPeriod | account_balance 期末余额 JOIN account_subject |
| 利润表 | selectProfitLossByPeriod | journal_entry_line 聚合（排除结转凭证） |
| 现金流量表 | selectCashFlowByPeriod | journal_entry_line JOIN aux（CASH_FLOW 维度） |

## 二、核心实现

### 2.1 资产负债表
`selectBalanceByPeriod` 读物化期末余额 + JOIN 科目字典取 `category`/`balance_direction`。
按 category 分组，金额归约：资产（含成本）= 借正贷负，负债/权益 = 贷正借负。
校验 `资产合计 = 负债合计 + 权益合计`。

### 2.2 利润表
`selectProfitLossByPeriod` 从流水聚合损益类科目发生额，**JOIN journal_entry 排除结转凭证**
（`description NOT LIKE '结转%'`）。净额：D 方向 = 借 - 贷（费用正），C 方向 = 贷 - 借（收入正）。
按固定科目编码映射计算营业利润 → 利润总额 → 净利润。

### 2.3 现金流量表（直接法）
`selectCashFlowByPeriod` 聚合现金科目（1001/1002/1012）按 CASH_FLOW 维度（CF001/002/003）：
流入 = 借方发生，流出 = 贷方发生。净流量 = 流入 - 流出。

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /report/balance-sheet?period=YYYYMM | 资产负债表 |
| GET | /report/income-statement?period=YYYYMM | 利润表 |
| GET | /report/cash-flow?period=YYYYMM | 现金流量表 |

---

## 四、关键坑与教训

1. **利润表不能读结账后物化值**：结账时损益类科目被结转凭证归零，
   `account_balance` 里损益科目 `period_debit == period_credit`（净额 0）。
   利润表必须读**结转前**的原始发生额 → 改为从 `journal_entry_line` 聚合，
   JOIN `journal_entry` 用 `description NOT LIKE '结转%'` 排除结转凭证。

2. **现金流量表依赖 CASH_FLOW 辅助核算**：现金科目（1001/1002/1012）必须在分录行
   挂 CASH_FLOW 维度，报表才能按经营/投资/筹资分类。否则现金流入流出无法归集到
   三大活动（这是现金流量表的本质要求）。

3. **资产负债表读物化值（O(1)）**：报表查询走 `account_balance` 物化值，不扫流水，
   这是 Phase 9 余额物化设计在报表层的收益——报表即时输出无需重算全量流水。
