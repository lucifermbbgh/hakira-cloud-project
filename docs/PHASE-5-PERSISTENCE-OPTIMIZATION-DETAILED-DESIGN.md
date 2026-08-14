# hakira-cloud-project — Phase 5 数据持久化与安全优化详细设计

> **阶段：** Phase 5 · 数据持久化与安全优化
> **状态：** ✅ 已完成

---

## 一、全局异常处理器设计

### 1.1 异常体系

```java
// 业务错误码枚举
BizErrorCode {
    ENTRY_UNBALANCED("1001", "借贷不平衡"),
    STOCK_INSUFFICIENT("1002", "库存不足"),
    ENTRY_NOT_FOUND("1003", "分录不存在"),
    ACCOUNT_NOT_FOUND("1004", "会计科目不存在"),
    DATA_VERSION_CONFLICT("1005", "数据已被修改"),
    SYSTEM_ERROR("88888", "系统异常")
}

// 业务异常（运行时）
BizException extends RuntimeException { errorCode; errorInfo; }

// 全局处理器
GlobalExceptionHandler (@RestControllerAdvice) {
    @ExceptionHandler(BizException.class) → Result.returnFail(code, info)
    @ExceptionHandler(IllegalArgumentException.class) → Result.returnFail(...)
    @ExceptionHandler(Exception.class) → Result.returnUnknown(...)
}
```

### 1.2 旧代码清理

清除 entry/auth 的 `BlockExceptionHandler`/`FallbackExceptionHandler`（Sentinel 降级回调，非真正的异常处理器），ConfigController 的 `@SentinelResource` 降级回调引用一并清理，限流异常改由 GlobalExceptionHandler 兜底。

## 二、BCrypt 设计

| 项 | 变更 |
|----|------|
| PasswordEncoder | NoOpPasswordEncoder → BCryptPasswordEncoder |
| 数据库密码 | admin123 → `$2a$10$...`（60 位哈希） |
| 生成方式 | BCryptPasswordEncoder（cost 10） |

## 三、表结构设计（核心）

### 3.1 五张表总览

| 表 | 类型 | 分区 | 说明 |
|----|------|------|------|
| `account_subject` | 字典表 | 否 | 国家标准会计科目 |
| `journal_entry` | 分录头 | 按月 by entry_date | 分录汇总 |
| `journal_entry_line` | 分录行 | 按月 by entry_date | 分录明细 |
| `stock_movement` | 流水表 | 按月 by movement_date | 库存变动事件 |
| `stock_snapshot` | 状态表 | 否 | 每物资当前库存 |

### 3.2 快照表 vs 流水表（类型说明）

| 维度 | 流水表（stock_movement） | 快照表（stock_snapshot） |
|------|------------------------|------------------------|
| 性质 | 事件表/明细表（append-only） | 状态表/汇总表（物化） |
| 行数 | 随业务持续增长 → 需分区 | 每物资一行，数量小 → 不分区 |
| 用途 | 记录每次变动，可追溯 | 快速查当前库存，避免每次 SUM |

### 3.3 主键与索引设计

**分区表约束：分区键必须在主键里**，故用复合主键（业务ID + 日期分区键）。

| 表 | 主键 | 索引 |
|----|------|------|
| journal_entry | (entry_id, entry_date) | idx_voucher(voucher_no)、idx_status_date(status, entry_date) |
| journal_entry_line | (line_id, entry_date) | idx_entry(entry_id, entry_date)、idx_subject(subject_code) |
| stock_movement | (movement_id, movement_date) | idx_item_date(item_code, movement_date) |
| stock_snapshot | (item_code) | — |
| account_subject | (subject_code) | — |

### 3.4 状态字段 + 乐观锁设计

每张业务表含：

| 字段 | 用途 |
|------|------|
| `status` | 状态机（分录 POSTED/REVERSED，库存 ACTIVE） |
| `version` | 乐观锁版本号，更新时 `WHERE version=?` 校验 |
| `create_time` / `update_time` | 审计字段，`ON UPDATE CURRENT_TIMESTAMP` |

**前状态检查（乐观锁）机制**：

```sql
UPDATE stock_snapshot
SET current_quantity = ?, version = version + 1
WHERE item_code = ? AND version = ?;   -- version 不匹配 → 影响行数 0 → 抛 1005
```

防止并发重复更新/删除导致数据错误。

### 3.5 会计科目表（国家标准）

预置 21 个国家标准科目（资产/负债/权益/成本/损益），分录的 subject_code 必须在科目表存在，否则抛 1004。

```
1001 库存现金  1002 银行存款  1122 应收账款  1403 原材料  1405 库存商品
1601 固定资产  2001 短期借款  2202 应付账款  2211 应付职工薪酬
4001 实收资本  4103 本年利润  4104 利润分配
5001 生产成本  5101 制造费用
6001 主营业务收入  6051 其他业务收入  6401 主营业务成本
6403 税金及附加  6601 销售费用  6602 管理费用  6603 财务费用
```

## 四、Service 改造设计

| 服务 | 改造 |
|------|------|
| EntryServiceImpl | ConcurrentHashMap → JournalEntryMapper（头）+ JournalEntryLineMapper（行），事务写入 + 科目校验 |
| StockServiceImpl | 内存 Map → StockMovementMapper（流水 append）+ StockSnapshotMapper（快照乐观锁更新） |

## 五、关键坑位

| 坑 | 处理 |
|----|------|
| mybatis-spring 与 Spring 6.1 不兼容（factoryBeanObjectType） | 显式 mybatis-spring 3.0.3 |
| mybatis 配置写在 spring.mybatis 下 | 移到顶层 mybatis（map-underscore-to-camel-case 才生效） |
| auth 冷启动首次登录慢 | Druid/MyBatis 连接池未就绪，非 bug |
