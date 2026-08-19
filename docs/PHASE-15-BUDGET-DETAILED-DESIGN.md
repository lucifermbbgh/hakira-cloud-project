# PHASE-15 预算管理 — 详细设计

> **版本：** v1.0 · 2026-08-15
> **前置：** PHASE-15-BUDGET-DESIGN.md

---

## 一、数据模型（新增 1 张表）

`budget`：`(period, subject_code)` 复合主键 + budget_amount + version 乐观锁。

## 二、核心实现

### 2.1 预算编制（setBudget）
`INSERT ... ON DUPLICATE KEY UPDATE`（upsert），重复编制覆盖 + version+1。

### 2.2 执行监控（query）
1. 查预算（selectByPeriod）
2. 查实际（selectActualByPeriod：聚合分录行按科目，JOIN 科目字典取方向）
3. 合并：实际发生额按方向归约（D=借−贷，C=贷−借），差异 = 实际 − 预算

### 2.3 差异分析（variance）
query 结果过滤 `overBudget=true` 的科目：
- 费用类（D）：差异 > 0 = 超支
- 收入类（C）：差异 < 0 = 未达标

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /budget/set | 编制/更新预算 |
| GET | /budget/query?period=YYYYMM | 执行监控 |
| GET | /budget/variance?period=YYYYMM | 差异分析 |

---

## 四、关键坑与教训

1. **期间编号年份**：`202509` 按 yyyyMM 解析为 **2025 年 9 月**，与 2026 年分录年份
   不匹配导致实际发生额恒为 0。验证时必须保证期间编号与分录日期同年（202609）。

2. **测试数据污染**：复用 202605 期间时，Phase 13 的赊销分录（贷 6001）污染了
   收入科目实际值（5600 而非 3000）。验证预算需用干净期间，或确认期间无历史数据。

3. **差异方向区分**：费用类超支（差异>0）与收入类未达标（差异<0）是两个方向，
   统一用 `overBudget` 布尔标识，前端据此提示。
