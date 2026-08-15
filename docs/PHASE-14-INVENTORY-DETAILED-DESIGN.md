# PHASE-14 存货核算与计价 — 详细设计

> **版本：** v1.0 · 2026-08-15
> **前置：** PHASE-14-INVENTORY-DESIGN.md

---

## 一、数据模型（stock 表加成本字段）

| 表 | 新增字段 | 说明 |
|----|---------|------|
| stock_snapshot | total_cost / weighted_avg_cost | 库存总成本 / 加权平均单价 |
| stock_movement | unit_cost / total_cost | 单价 / 本次总成本 |

`direction` 字段由 varchar(8) 加长为 varchar(20)（容纳 STOCKTAKE_GAIN/LOSS）。

## 二、核心实现

### 2.1 移动加权平均计价
- **入库**：`新加权平均 = (旧总成本 + 入库成本) / (旧数量 + 入库数量)`
- **出库**：`出库成本 = 数量 × 当前加权平均单价`，单价不变，总成本减少

### 2.2 盘点（stocktake）
- 差异 = 实际数量 − 账面数量（>0 盘盈 STOCKTAKE_GAIN，<0 盘亏 STOCKTAKE_LOSS）
- 成本差异 = 差异 × 加权平均单价，快照数量与成本同步调整

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /stock/inbound | 入库（含 unitCost） |
| POST | /stock/outbound | 出库（自动计算成本） |
| POST | /stock/stocktake | 盘点（盘盈盘亏） |
| GET | /stock/snapshot/{itemCode} | 库存余额（含成本） |
| GET | /stock/movements/{itemCode} | 收发流水（含单价/成本） |

---

## 四、关键坑与教训

1. **direction 字段长度**：原 varchar(8) 无法容纳 `STOCKTAKE_GAIN`（13 字符），
   INSERT 时 `Data too long for column 'direction'` 截断报错。已 MODIFY 为 varchar(20)。

2. **移动加权平均单价出库不变**：出库只减少数量与总成本，单价保持不变（这是移动
   加权平均的特性，区别于月末一次加权平均）。

3. **FIFO 未实现**：先进先出需跟踪批次成本，本阶段仅实现移动加权平均，FIFO 标记后续。

4. **出库成本结转凭证未接**：出库成本已计算，但生成结转凭证（借 6401/贷 1405）
   需跨服务调 entry，本阶段先记录成本，凭证生成后续补充。
