# hakira-cloud-project — Phase 5 数据持久化与安全优化方案设计

> **阶段：** Phase 5 · 数据持久化与安全优化
> **状态：** ✅ 已完成
> **关联提交：** ed51e907 · c02e73af

---

## 一、阶段目标

解决 Phase 4 遗留的三类问题：**安全（明文密码）、异常处理（500）、持久化（内存存储）**，为 Phase 6 大数据分析提供数据源。

## 二、优化清单

| # | 优化项 | 解决的问题 | 规模 |
|---|--------|-----------|------|
| 1 | 全局异常处理器 | 借贷不平衡/库存不足返回 500 → 业务错误码 | 小 |
| 2 | BCrypt 密码哈希 | NoOpPasswordEncoder 明文密码 → 哈希 | 小 |
| 3 | 内存存储换 DB | entry/stock 重启丢数据 → MySQL 分区表 | 大 |

## 三、执行顺序

```
步骤1 全局异常处理器（~0.5h 独立）
  → 步骤2 BCrypt（~0.5h 独立）
  → 步骤3 内存换 DB（~2-4h 核心，依赖前两步）
```

理由：异常处理器是错误返回规范底座（DB 持久化引入更多错误场景）；BCrypt 独立简单；DB 持久化依赖最多，最后集中做。

## 四、设计要点

### 4.1 全局异常处理器

- 新增 `BizException`（运行时异常，携带错误码）+ `BizErrorCode`（错误码枚举）
- `GlobalExceptionHandler`（@RestControllerAdvice）统一捕获，返回 Result 结构
- 清除旧的 Sentinel 降级回调（BlockExceptionHandler/FallbackExceptionHandler）

### 4.2 BCrypt

- `NoOpPasswordEncoder` → `BCryptPasswordEncoder`
- 数据库 admin 密码从明文 → `$2a$10$` 60 位哈希

### 4.3 表结构（分区 + 乐观锁 + 科目表）

- 5 张表：会计科目 + 分录头/行 + 库存流水/快照
- 流水/分录按月 RANGE 分区（YYYYMM 形式）
- 每表 status + version + create_time/update_time（乐观锁防并发）

## 五、阶段验收标准

- [x] 业务错误码 1001/1002/1004 返回（HTTP 200 非 500）
- [x] BCrypt 登录成功（哈希密码匹配）
- [x] 分录/库存持久化 DB，重启数据不丢
- [x] 分录科目合法性校验（科目须在科目表存在）
