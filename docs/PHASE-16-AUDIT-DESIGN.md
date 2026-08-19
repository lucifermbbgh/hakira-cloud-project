# PHASE-16 审计合规与系统治理 — 设计文档

> **版本：** v1.0 · 2026-08-15
> **阶段：** Phase 16（横切贯穿，最后一个阶段）
> **模块：** hakira-ledger-entry（账务域）

---

## 一、阶段目标

审计追溯与系统治理：操作审计日志、数据全链路追溯。

| 功能 | 说明 |
|------|------|
| 操作审计日志 | 记录关键操作（谁/何时/做了什么） |
| 数据全链路追溯 | 凭证 → 分录行 → 辅助核算 → 库存流水 |

> RBAC 权限细化、多租户隔离标记为后续（跨服务，工作量大，本阶段先做审计追溯）。

---

## 二、核心概念

### 2.1 操作审计日志
关键操作（分录录入/冲销/结账）自动记录：操作类型、操作人、实体、详情、时间。

### 2.2 数据全链路追溯
从凭证号反查完整链路：
```
journal_entry（凭证头）
  → journal_entry_line（分录行）
    → journal_entry_line_aux（辅助核算维度）
  → stock_movement（库存流水，按 related_voucher_no 关联）
```

---

## 三、数据模型（新增 1 张表）

### audit_log（操作审计日志）

| 字段 | 类型 | 说明 |
|------|------|------|
| log_id | bigint | 自增主键 |
| operation | varchar(64) | 操作类型（POST_ENTRY/REVERSE_ENTRY/CLOSE_PERIOD） |
| operator | varchar(64) | 操作人 |
| entity_type | varchar(32) | 实体类型 |
| entity_id | varchar(64) | 实体 ID |
| detail | varchar(500) | 详情 |
| create_time | datetime | 操作时间 |

---

## 四、功能设计

### 4.1 操作审计日志
- `AuditService.record(...)` 记录日志（在分录录入/冲销/月结关键节点调用）
- `GET /audit/logs` 查询审计日志

### 4.2 数据全链路追溯（GET /audit/trace/{entryId}）
输入凭证 ID，输出：凭证头 → 分录行（含辅助核算）→ 关联库存流水。

---

## 五、接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /audit/logs?limit=N | 审计日志查询 |
| GET | /audit/trace/{entryId} | 数据全链路追溯 |

---

## 六、验证

见 PHASE-16-AUDIT-TEST-REPORT.md：录入分录触发审计日志 → 查询日志 →
追溯凭证完整链路（头+行+辅助核算+流水）。

---

## 七、后续规划（RBAC 权限 + 多租户，⬜ 待单独规划）

> 本阶段聚焦审计追溯；权限细化与多租户隔离为系统级跨服务改造，待单独规划。

| 项 | 说明 | 涉及范围 |
|----|------|---------|
| RBAC 权限 | 用户-角色-权限模型 + auth 服务扩展 + 接口鉴权 | auth + gateway + 各服务注解 |
| 多租户隔离 | 所有业务表加 tenant_id + 查询过滤 | 全模块 Mapper 改造 |
