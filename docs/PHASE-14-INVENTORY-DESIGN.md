# PHASE-14 存货核算与计价 — 设计文档

> **版本：** v1.0 · 2026-08-15
> **阶段：** Phase 14（依赖 Phase 5 库存流水/快照 + Phase 11 成本核算）
> **模块：** hakira-ledger-stock（库存域）

---

## 一、阶段目标

存货成本核算：存货计价（移动加权平均）、存货盘点（盘盈盘亏）、出库成本结转。

| 功能 | 说明 |
|------|------|
| 存货计价 | 移动加权平均单价，入库/出库自动更新 |
| 存货盘点 | 盘盈盘亏，调整数量与成本 |
| 出库成本 | 出库按加权平均单价计算成本 |

---

## 二、核心概念

### 2.1 移动加权平均（本阶段实现）
```
入库：new_avg = (旧总成本 + 本次入库成本) / (旧数量 + 本次入库数量)
出库：出库成本 = 出库数量 × 当前加权平均单价
     新总成本 = 旧总成本 − 出库成本
```

### 2.2 FIFO（✅ 已完成）
先进先出：新增 `inventory_lot` 批次表，入库创建批次（记录单价+剩余量），
出库按最早批次顺序扣减。`stock_snapshot.costing_method` 字段控制计价方法
（WEIGHTED_AVG 加权平均 / FIFO 先进先出）。

### 2.3 存货盘点
| 情形 | 分录 |
|------|------|
| 盘盈（实际>账面） | 借 1405 库存商品 / 贷 1901 待处理财产损溢 |
| 盘亏（实际<账面） | 借 1901 待处理财产损溢 / 贷 1405 库存商品 |

---

## 三、数据模型（stock_snapshot / stock_movement 加成本字段）

| 表 | 新增字段 | 说明 |
|----|---------|------|
| stock_snapshot | total_cost decimal(18,2) | 库存总成本 |
| stock_snapshot | weighted_avg_cost decimal(18,4) | 加权平均单价 |
| stock_movement | unit_cost decimal(18,4) | 单价（入库传入/出库=加权平均） |
| stock_movement | total_cost decimal(18,2) | 本次总成本 |

---

## 四、功能设计

### 4.1 入库（recordInbound，加 unitCost）
入库时记录单价，更新快照：数量 += 入库量，总成本 += 入库成本，重算加权平均单价。

### 4.2 出库（recordOutbound）
出库时按当前加权平均单价计算成本，记录出库成本，更新快照（数量/总成本减少）。

### 4.3 盘点（stocktake）
传入实际数量，计算盘盈/盘亏，按加权平均单价调整快照数量与成本，
记录盘盈盘亏流水（direction 标记 STOCKTAKE_GAIN / STOCKTAKE_LOSS）。

### 4.4 出库成本结转凭证（✅ 已完成）
出库时自动生成结转凭证（借 6401 主营业务成本 / 贷 1405 库存商品），
金额 = 出库数量 × 加权平均单价。stock 与 entry 同库，直接写 journal_entry 表
（CostTransferMapper），出库事务内原子提交。

---

## 五、接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /stock/inbound | 入库（含 unitCost） |
| POST | /stock/outbound | 出库（自动计算成本） |
| POST | /stock/stocktake | 盘点（盘盈盘亏） |
| GET | /stock/snapshot/{itemCode} | 库存余额（含成本） |

---

## 六、验证

见 PHASE-14-INVENTORY-TEST-REPORT.md：入库两次不同单价 → 加权平均单价更新 →
出库成本计算 → 盘点盘盈盘亏。
