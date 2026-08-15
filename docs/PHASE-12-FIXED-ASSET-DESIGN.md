# PHASE-12 固定资产管理 — 设计文档

> **版本：** v1.0 · 2026-08-15
> **阶段：** Phase 12（依赖 Phase 7 科目体系）
> **模块：** hakira-ledger-entry（账务域）

---

## 一、阶段目标

固定资产全生命周期：资产卡片、折旧计提、资产处置/报废。

| 功能 | 说明 |
|------|------|
| 资产卡片 | 登记/查询固定资产（原值、残值率、折旧年限、折旧方法） |
| 折旧计提 | 按月计提折旧，自动生成折旧凭证，更新累计折旧/净值 |
| 资产处置 | 处置/报废，生成清理凭证，更新资产状态 |

---

## 二、核心概念

### 2.1 折旧方法
| 方法 | 月折旧额公式 |
|------|-------------|
| 直线法 STRAIGHT_LINE | 原值 × (1 − 残值率) / 折旧月数 |
| 双倍余额递减 DOUBLE_DECLINING | 净值 × (2 / 折旧月数)，不低于残值 |

### 2.2 折旧凭证（每月）
```
借 6602 管理费用（折旧费用）
贷 1602 累计折旧
```
计提后：累计折旧增加、净值减少（净值 = 原值 − 累计折旧）。

### 2.3 处置凭证
```
借 1602 累计折旧（已计提额）
借 6711 营业外支出（净值损失）
贷 1601 固定资产（原值）
```
处置后资产状态置 DISPOSED。

---

## 三、数据模型（新增 1 张表）

### fixed_asset（固定资产卡片）

| 字段 | 类型 | 说明 |
|------|------|------|
| asset_code | varchar(32) | 资产编码（主键） |
| asset_name | varchar(100) | 资产名称 |
| category | varchar(32) | 资产类别 |
| original_value | decimal(18,2) | 原值 |
| residual_rate | decimal(5,4) | 残值率（默认 0.05） |
| useful_life | int | 折旧年限（月） |
| depreciation_method | varchar(32) | STRAIGHT_LINE / DOUBLE_DECLINING |
| accumulated_depreciation | decimal(18,2) | 累计折旧 |
| net_value | decimal(18,2) | 净值 |
| status | varchar(16) | IN_USE / DISPOSED |
| version | int | 乐观锁 |

---

## 四、功能设计

### 4.1 资产卡片登记（POST /asset/create）
登记资产，净值 = 原值，累计折旧 = 0。

### 4.2 折旧计提（POST /asset/depreciate?period=YYYYMM）
遍历 IN_USE 资产，按折旧方法计算月折旧额，生成折旧凭证，更新累计折旧 + 净值
（乐观锁）。净额 ≤ 残值时停止计提。

### 4.3 资产处置（POST /asset/dispose/{assetCode}）
生成清理凭证，更新状态 DISPOSED（乐观锁）。

---

## 五、接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /asset/create | 登记资产卡片 |
| GET | /asset/{assetCode} | 查询资产 |
| GET | /asset/list | 资产列表 |
| POST | /asset/depreciate?period=YYYYMM | 折旧计提 |
| POST | /asset/dispose/{assetCode} | 资产处置 |

---

## 六、验证

见 PHASE-12-FIXED-ASSET-TEST-REPORT.md：登记资产 → 折旧计提（直线法/双倍余额递减）
→ 资产处置，验证累计折旧/净值更新 + 折旧/处置凭证。
