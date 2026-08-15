# hakira-cloud-project — Phase 8 凭证管理深化详细设计

> **阶段：** Phase 8 · 凭证管理深化（状态机/冲销/自动编号）
> **状态：** ✅ 已完成（审核流对接 Flowable 后续单独做）

---

## 一、凭证状态机

| 状态 | 含义 | 触发 |
|------|------|------|
| POSTED | 已记账（录入默认态） | 录入即 POSTED |
| REVERSED | 已冲销 | 冲销后原分录置此态 |
| VOID | 已作废（预留） | 后续作废接口 |

> DRAFT/PENDING 态留待审核流（对接 Flowable）阶段实现。

## 二、冲销设计（reverseEntry）

### 2.1 流程

```
POST /entry/reverse/{entryId}
  1. 查原分录（不存在 → 1003）
  2. 校验状态 == POSTED（否则 → 1008）
  3. 查原分录行 + 辅助核算（按 lineId 分组）
  4. 生成反向分录头（借贷合计互换，凭证号自动生成）
  5. 写反向分录行（借贷金额互换 + 复制辅助核算维度）
  6. 原分录置 REVERSED（乐观锁 version，冲突 → 1005）
```

### 2.2 反向规则

| 项 | 原分录 | 冲销分录 |
|----|--------|---------|
| 借方金额 | debitAmount | = 原 creditAmount |
| 贷方金额 | creditAmount | = 原 debitAmount |
| 科目 | subject_code | 不变 |
| 辅助核算 | aux | 完整复制 |
| 状态 | POSTED → REVERSED | POSTED |

## 三、凭证号自动编号

规则：`PZ-YYYYMMDD-NNN`（PZ + 日期 + 当日 3 位流水号）。

- 未传 voucherNo 时自动生成
- 查询当日最大凭证号 `selectMaxVoucherNo`（LIKE 'PZ-日期-%'），序号 +1

## 四、代码改动

| 文件 | 改动 |
|------|------|
| BizErrorCode | 加 1008 ENTRY_STATUS_INVALID |
| IEntryService | 加 reverseEntry(entryId) |
| EntryController | 加 POST /entry/reverse/{entryId} |
| JournalEntryMapper | 加 updateStatus（乐观锁）+ selectMaxVoucherNo |
| EntryServiceImpl | reverseEntry + generateVoucherNo + postEntry 自动编号 |

## 五、乐观锁状态校验

- 冲销时 `updateStatus(entryId, REVERSED, version)`：`WHERE entry_id=? AND version=?`
- version 不匹配 → 影响行数 0 → 抛 1005（防并发重复冲销）

## 六、遗留

| 项 | 说明 |
|----|------|
| 审核流 | 对接 Phase 3 Flowable（提交/通过/驳回），后续单独做 |
| VOID 作废 | 预留状态，作废接口后续补 |
| DRAFT 草稿 | 配合审核流实现 |
