# PHASE-13 应收应付与往来账龄 — 设计文档

> **版本：** v1.0 · 2026-08-15
> **阶段：** Phase 13（依赖 Phase 7 辅助核算 + Phase 8 凭证管理）
> **模块：** hakira-ledger-entry（账务域）

---

## 一、阶段目标

往来核算与账龄管理：往来单位、应收账款/应付账款账龄分析、超龄应收预警。

| 功能 | 说明 |
|------|------|
| 往来单位 | 客户（CUSTOMER）/ 供应商（SUPPLIER）列表 |
| 账龄分析 | 应收/应付按账龄段（30/60/90 天）汇总 |
| 超龄预警 | 90 天以上应收/应付标识 |

---

## 二、核心概念

### 2.1 往来科目与维度
| 类型 | 科目 | 辅助核算维度 |
|------|------|-------------|
| 应收账款 | 1122 应收账款 | CUSTOMER 客户 |
| 应付账款 | 2202 应付账款 | SUPPLIER 供应商 |

往来单位复用 Phase 7 辅助核算维度值（auxiliary_value），不新建表。

### 2.2 账龄段
| 段 | 天数 |
|----|------|
| 30 天内 | 0–30 |
| 30–60 天 | 30–60 |
| 60–90 天 | 60–90 |
| 90 天以上 | >90（预警） |

账龄天数 = 截止日期 − 业务发生日期（entry_date）。

---

## 三、数据模型（复用，不新增表）

- 分录行 `journal_entry_line` + `journal_entry_line_aux`（CUSTOMER/SUPPLIER 维度）
- 维度值 `auxiliary_value`（往来单位）

---

## 四、功能设计

### 4.1 往来单位列表（GET /partner/list?dimension=CUSTOMER）
查询 auxiliary_value 表的维度值（客户/供应商）。

### 4.2 应收账款账龄（GET /aging/receivable?asOf=YYYY-MM-DD）
聚合 1122 挂 CUSTOMER 维度的分录，按客户分组，借方发生按账龄段归类。

### 4.3 应付账款账龄（GET /aging/payable?asOf=YYYY-MM-DD）
聚合 2202 挂 SUPPLIER 维度的分录，按供应商分组，贷方发生按账龄段归类。

---

## 五、接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /partner/list?dimension=CUSTOMER | 往来单位列表 |
| GET | /aging/receivable?asOf=YYYY-MM-DD | 应收账款账龄 |
| GET | /aging/payable?asOf=YYYY-MM-DD | 应付账款账龄 |

---

## 六、验证

见 PHASE-13-AGING-TEST-REPORT.md：录入不同日期的应收/应付分录（挂 CUSTOMER/SUPPLIER 维度），
验证账龄段归类 + 90 天以上预警。
