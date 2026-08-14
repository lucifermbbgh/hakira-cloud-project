# hakira-cloud-project — Phase 2 会计复式记账核心业务详细设计

> **阶段：** Phase 2 · 会计复式记账核心业务
> **状态：** ✅ 已完成

---

## 一、模块分层

| 模块 | 文件 | 职责 |
|------|------|------|
| 分录服务 | `entry/controller/EntryController.java` | `/entry` 接口 |
| | `entry/service/impl/EntryServiceImpl.java` | 借贷平衡校验 + 分录存储 |
| 库存服务 | `stock/controller/StockController.java` | `/stock` 接口 |
| | `stock/service/StockServiceImpl.java` | 入出库 + 快照逻辑 |
| 接口契约 | `hakira-ledger-api` | `IEntryService`/`IStockService` + DTO |

## 二、核心数据模型（DTO）

### 2.1 分录请求/响应

```java
// 请求
JournalEntryRequest {
    voucherNo; entryDate; description;
    List<EntryLine> entries;   // 分录行
    EntryLine { accountCode; accountName; description; debitAmount; creditAmount; }
}
// 响应
JournalEntryResponse {
    entryId; voucherNo; entryDate; description;
    totalDebit; totalCredit; status;   // POSTED
    List<EntryLineResponse> entries;
}
```

### 2.2 库存请求/响应

```java
StockMovementRequest { itemCode; itemName; quantity; unit; relatedVoucherNo; remark; }
StockMovementResponse { movementId; itemCode; itemName; direction; quantity; unit; relatedVoucherNo; movementDate; remark; }
StockSnapshotResponse { itemCode; itemName; currentQuantity; unit; lastUpdateTime; }
```

## 三、业务逻辑设计

### 3.1 分录记账（postEntry）

1. 遍历分录行，累加借方合计、贷方合计
2. 借贷不平衡 → 抛异常拒绝
3. 生成雪花ID → 构建响应（POSTED）→ 存入内存 Map
4. 返回分录响应

### 3.2 库存入库/出库

| 操作 | 逻辑 |
|------|------|
| 入库 | `current += quantity`，记录 INBOUND 流水 |
| 出库 | `current < quantity` 则拒绝，否则 `current -= quantity`，记录 OUTBOUND 流水 |
| 快照 | 返回当前库存 + 最后更新时间 |
| 流水 | 追加到流水列表，按时间倒序 |

## 四、关键实现细节

| 细节 | 说明 |
|------|------|
| 存储结构 | `ConcurrentHashMap<String, JournalEntryResponse>`（分录）、`ConcurrentHashMap<String, BigDecimal>`（库存） |
| 线程安全 | ConcurrentHashMap 保证并发读写 |
| 状态字段 | 分录 status=POSTED，Phase 5 扩展为完整状态机 |

> 注：本阶段为内存存储，重启数据丢失；Phase 5 换成 MySQL 分区表持久化。
