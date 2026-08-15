# PHASE-9 期末结账与账务结转 — 详细设计

> **版本：** v1.0 · 2026-08-15
> **前置：** PHASE-9-PERIOD-END-CLOSING-DESIGN.md

---

## 一、新增数据表

### 1.1 accounting_period（会计期间状态）
`period` 主键；`status` OPEN/CLOSING/CLOSED；`version` 乐观锁。
首次录入分录时由 `checkPeriodWritable` 自动 `INSERT IGNORE` 创建 OPEN 记录。

### 1.2 account_balance（科目余额物化）
`(period, subject_code)` 复合主键。六个金额字段：opening_debit/credit（期初）、
period_debit/credit（本期发生）、closing_debit/credit（期末）。

---

## 二、核心实现

### 2.1 状态机
```
OPEN ──close──> CLOSING ──完成──> CLOSED
                    └──异常回滚──> OPEN
```
`close` 用乐观锁 `updateStatus WHERE version=?`，防并发重复结账。

### 2.2 损益结转（doProfitTransfer）
1. `aggregateProfitLoss` 聚合损益类科目（category=损益）本期发生额 + balance_direction
2. 收入类（C）：净贷方 = 贷 - 借 > 0 才结转；费用类（D）：净借方 = 借 - 贷 > 0 才结转
3. 生成两张结转凭证（entry_date = 期间末日，voucher_no 自动编号）：
   - 结转收入：多借收入科目 + 一贷 4103 本年利润
   - 结转费用：一借 4103 + 多贷费用科目
4. 结转凭证本身也属于本期发生，物化时自然让损益科目归零、本年利润 = 净利润

### 2.3 月结（close）
```
0. 结账顺序约束：上一期间必须 CLOSED（期初承接依赖）
1. OPEN → CLOSING（乐观锁）
2. 损益结转（未结转才执行；已单独结转则跳过复用）
3. 物化全科目余额（期初承接 + 本期发生 + 期末归约）
4. CLOSING → CLOSED
```
整体 `@Transactional`，任一步失败回滚到 OPEN。

### 2.4 科目余额物化（materializeBalance）
- 期初 = 上一期间物化的期末（`prevPeriod` = 上个月 YYYYMM）
- 期末归约：`net = opening_debit - opening_credit + period_debit - period_credit`，
  net > 0 → closing_debit，net < 0 → closing_credit
- 期初有余额但本期无发生的科目，结转期末=期初

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /closing/balance?period=YYYYMM | 科目余额表（物化优先，未结账实时聚合） |
| GET | /closing/trial-balance?period=YYYYMM | 试算平衡表 |
| POST | /closing/profit-transfer?period=YYYYMM | 损益结转（幂等） |
| POST | /closing/close?period=YYYYMM | 一键月结 |

错误码：1009 期间已结账、1010 期间状态不允许（含乱序结账）。

---

## 四、关键坑与教训

1. **BigDecimal 序列化**：`1000.00` 经 Jackson 序列化为 `1000.0`（尾随零丢失），
   验证脚本断言勿写死 `"1000.00"`，用 `Decimal` 精确比较。

2. **期初承接时序**：乱序结账（先 11 月后 10 月）导致 11 月期初承接"过时"。
   这是真实正确性问题 → 加**结账顺序约束**（上一期间未结账则拒绝）。

3. **期间记录缺失**：录入分录不建期间记录时，乱序约束依赖的 `accounting_period`
   记录可能不存在 → `checkPeriodWritable` 首次录入时 `INSERT IGNORE` 自动建 OPEN。

4. **close 复用 doProfitTransfer**：`close` 内部调 `doProfitTransfer` 会撞上幂等检查
   （先单独 profitTransfer 再 close 时）→ close 里改为「未结转才执行，已结转跳过」。

5. **聚合下推 + 分区裁剪**：所有余额/结转聚合均 `SUM GROUP BY` + `entry_date` 范围，
   命中按月分区，避免全表扫描和应用层累加。
