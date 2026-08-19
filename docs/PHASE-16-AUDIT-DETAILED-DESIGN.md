# PHASE-16 审计合规与系统治理 — 详细设计

> **版本：** v1.0 · 2026-08-15
> **前置：** PHASE-16-AUDIT-DESIGN.md

---

## 一、数据模型（新增 1 张表）

`audit_log`：自增主键 + operation/operator/entity_type/entity_id/detail/create_time，
建 entity + create_time 索引便于按实体/时间检索。

## 二、核心实现

### 2.1 操作审计日志
`AuditService.record(...)` 记录日志，在关键操作节点调用：
- 分录录入 → `POST_ENTRY`
- 凭证冲销 → `REVERSE_ENTRY`
- 月结 → `CLOSE_PERIOD`

审计日志在业务事务内写入（与业务操作原子提交，回滚则日志不残留）。

### 2.2 数据全链路追溯（trace）
```
journal_entry（凭证头）
  → journal_entry_line（分录行）
    → journal_entry_line_aux（辅助核算维度，按 lineId 分组）
  → stock_movement（库存流水，按 related_voucher_no 关联凭证号）
```

---

## 三、接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /audit/logs?limit=N | 审计日志查询（倒序） |
| GET | /audit/trace/{entryId} | 数据全链路追溯 |

---

## 四、关键坑与教训

1. **跨模块追溯库存流水**：stock_movement 表在 stock 模块，但 entry 与 stock 连同一
   个 MySQL 库，故 entry 可直接 SQL 查 stock_movement（按 related_voucher_no 关联），
   无需跨服务调用。

2. **审计日志与业务原子性**：record 无独立 @Transactional，依赖调用方事务，业务
   回滚时审计日志一并回滚，避免「日志残留但业务未发生」的假审计。

3. **RBAC 权限 / 多租户标记后续**：需跨服务（auth 网关 + 数据隔离），本阶段聚焦
   审计追溯，权限细化与多租户后续单独做。
