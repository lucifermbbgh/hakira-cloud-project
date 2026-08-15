# PHASE-9 期末结账与账务结转 — 设计文档

> **版本：** v1.0 · 2026-08-15
> **阶段：** Phase 9（依赖 Phase 7 科目体系 + Phase 8 凭证管理）
> **模块：** hakira-ledger-entry（账务域）

---

## 一、阶段目标

期末账务处理自动化：损益结转、月结锁定、试算平衡、科目余额表。

| 功能 | 说明 |
|------|------|
| 科目余额表 | 每个科目 期初 / 本期发生 / 期末余额 |
| 试算平衡表 | 校验全量科目借贷平衡（发生额 + 余额） |
| 损益结转 | 损益类科目余额自动结转到「本年利润」，生成结转凭证 |
| 月结流程 | 结转 + 余额物化 + 会计期间锁定 |

---

## 二、核心概念

### 2.1 会计期间
按月划分（`YYYYMM`）。期间状态机：

```
OPEN ──开始结账──> CLOSING ──完成──> CLOSED
                         │
                         └──失败回滚──> OPEN
```

- `OPEN`：正常记账期，允许录入分录
- `CLOSING`：结账进行中，禁止并发录入
- `CLOSED`：已结账，拒绝一切分录写入

### 2.2 损益结转
期末将损益类科目余额归零，转入「本年利润（4103）」：

- **收入类**（balance_direction=C）：贷方余额 → 借收入科目、贷本年利润
- **费用类**（balance_direction=D）：借方余额 → 借本年利润、贷费用科目

结转后损益类科目余额归零，本年利润余额 = 净利润。

### 2.3 余额物化（关键设计）
科目余额表**不实时 `SUM(流水)`**，而是结账时一次性算好并物化到 `account_balance`，
查询直接读物化值。这是海量流水场景下账务系统的标准总账（GL）设计。

---

## 三、数据模型（新增 2 张表）

### 3.1 accounting_period（会计期间状态）

| 字段 | 类型 | 说明 |
|------|------|------|
| period | varchar(6) | 会计期间 YYYYMM（主键） |
| status | varchar(16) | OPEN / CLOSING / CLOSED |
| closed_by / closed_at | — | 结账人 / 结账时间 |
| version | int | 乐观锁 |

### 3.2 account_balance（科目余额物化表）

| 字段 | 类型 | 说明 |
|------|------|------|
| period + subject_code | — | 复合主键（期间 + 科目） |
| opening_debit / opening_credit | decimal(18,2) | 期初借 / 贷方余额 |
| period_debit / period_credit | decimal(18,2) | 本期借 / 贷方发生额 |
| closing_debit / closing_credit | decimal(18,2) | 期末借 / 贷方余额 |

期初余额 = 上一期间物化的期末余额（首期 = 0）；期末余额 = 期初 + 本期发生（按方向）。

---

## 四、功能设计

### 4.1 科目余额表（GET /closing/balance）
读 `account_balance` 物化值（O(1) 查询，不扫流水）。若期间未结账，则实时聚合（`SUM GROUP BY`）。

### 4.2 试算平衡表（GET /closing/trial-balance）
校验：本期借方发生合计 = 本期贷方发生合计；期末借方余额合计 = 期末贷方余额合计。
不平衡则返回差额明细。

### 4.3 损益结转（POST /closing/profit-transfer）
1. 聚合损益类科目（category=损益）本期发生额
2. 计算各损益科目净余额（收入=贷-借，费用=借-贷）
3. 生成结转凭证（收入结转凭证：多借一贷；费用结转凭证：一借多贷），状态 POSTED，凭证号自动编号
4. 结转结果物化到 account_balance（损益科目本期发生/期末余额）

### 4.4 月结（POST /closing/close）
1. 检查期间 OPEN → 置 CLOSING（乐观锁）
2. 损益结转（生成结转凭证）
3. 生成全科目余额（物化 account_balance，含期初承接）
4. 置 CLOSED
5. 整体一个事务，失败回滚到 OPEN

---

## 五、接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /closing/balance?period=202608 | 科目余额表 |
| GET | /closing/trial-balance?period=202608 | 试算平衡表 |
| POST | /closing/profit-transfer?period=202608 | 损益结转 |
| POST | /closing/close?period=202608 | 月结（结转 + 物化 + 锁定） |

---

## 六、高并发 / 大数据量账务处理考量（重点）

期末结账是账务处理压力最集中的场景（全量流水汇总、批量结转、期间切换）。本阶段的并发考量：

### 6.1 余额物化，避免实时 SUM（核心）
- **问题**：科目余额表若每次 `SUM(流水)`，海量流水下查询放大、热点读竞争。
- **方案**：结账时一次性算出期末余额，物化到 `account_balance`；查询走物化值（O(1)），
  不扫流水。类比库存快照表（stock_snapshot），是「状态表 + 流水表」分离在总账域的推广。

### 6.2 聚合下推数据库
- **问题**：把全量分录行拉到应用层逐行累加，网络 + 内存双重压力。
- **方案**：`INSERT ... SELECT SUM(debit), SUM(credit) ... GROUP BY subject_code`
  在数据库端聚合，应用层只接收聚合结果。

### 6.3 分区裁剪
- 所有余额/结转查询均带 `entry_date` 范围条件，命中按月 RANGE 分区裁剪，
  只扫目标期间的分区，避免全表扫描。

### 6.4 期间锁定（乐观锁 + 状态机）
- 结账前置 `accounting_period` 为 CLOSING，防止结账过程中并发录入；
  CLOSED 期间录入分录直接拒绝（错误码 1009）。
- 分录写入（postEntry）增加期间状态校验，从源头挡住「已结账期间」的写入。

### 6.5 结转幂等
- 损益结转前检查期间状态：CLOSED 或已结转则拒绝，防止重复生成结转凭证。

### 6.6 事务边界
- 损益结转凭证 + 余额物化 + 期间锁定，同一 `@Transactional` 原子提交，
  任一步失败整体回滚到 OPEN，保证「要么全部结账成功，要么完全未结账」。

### 6.7 演进预留
- 单账户热点（余额表单行竞争）：后续可对 account_balance 按科目维度分片；
- 瞬时洪峰：后续接入 RocketMQ 削峰（顺序消息按期间/科目分区串行化结账任务）。

---

## 七、错误码（新增）

| 错误码 | 含义 |
|--------|------|
| 1009 | 会计期间已结账，拒绝分录写入 |
| 1010 | 会计期间状态不允许当前操作 |

---

## 八、依赖与产出

- **依赖**：Phase 7（科目表 balance_direction/category）、Phase 8（凭证号自动编号、状态机）
- **产出**：`accounting_period` + `account_balance` 表、ClosingController/Service、结账接口
- **验证**：见 PHASE-9-PERIOD-END-CLOSING-TEST-REPORT.md
