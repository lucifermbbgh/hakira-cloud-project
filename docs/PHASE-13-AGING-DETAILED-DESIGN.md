# PHASE-13 应收应付与往来账龄 — 详细设计

> **版本：** v1.0 · 2026-08-15
> **前置：** PHASE-13-AGING-DESIGN.md

---

## 一、数据模型（复用，不新增表）

- 往来单位 = Phase 7 辅助核算维度值（CUSTOMER 客户 / SUPPLIER 供应商）
- 账龄数据 = 分录行 `journal_entry_line` JOIN `journal_entry_line_aux`（往来维度）
  JOIN `auxiliary_value`（取单位名称）

## 二、核心实现

### 2.1 账龄段划分
| 段 | 条件 | 字段 |
|----|------|------|
| 30 天内 | days ≤ 30 | aging30 |
| 30-60 天 | 30 < days ≤ 60 | aging60 |
| 60-90 天 | 60 < days ≤ 90 | aging90 |
| 90 天以上 | days > 90 | agingOver90（预警） |

`days = asOf − entry_date`。

### 2.2 余额方向
- 应收（1122 挂 CUSTOMER）：净额 = 借 − 贷
- 应付（2202 挂 SUPPLIER）：净额 = 贷 − 借

### 2.3 聚合
`selectAgingDetails` 按往来单位 + 发生日期 GROUP BY，Service 层按账龄段归类汇总。

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /partner/list?dimension=CUSTOMER | 往来单位列表 |
| GET | /aging/receivable?asOf=YYYY-MM-DD | 应收账款账龄 |
| GET | /aging/payable?asOf=YYYY-MM-DD | 应付账款账龄 |

---

## 四、关键坑与教训

1. **期间锁定与测试数据冲突**：Phase 9 结账后 202607 期间 CLOSED，账龄测试录入
   7 月日期分录被期间锁定拦截（1009），导致部分数据静默未写入。验证前需确认
   目标日期落在 OPEN 期间（或用未结账的 5-6 月日期）。

2. **账龄分析读明细而非余额物化**：账龄依赖逐笔发生日期（entry_date），不能读
   account_balance 物化值（无日期维度），必须从分录行 + 辅助核算维度聚合。

3. **往来单位复用辅助核算框架**：CUSTOMER/SUPPLIER 维度复用 Phase 7 框架，
   与 CASH_FLOW（现金流）、COST_ITEM（成本）同构，验证了辅助核算的多场景扩展性。
