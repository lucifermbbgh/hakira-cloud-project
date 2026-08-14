# hakira-cloud-project — 接口文档

> **版本：** v1.0 · 2026-08-14
> **网关入口：** `http://localhost:9000`（统一路由 + JWT 校验）
> **直连端口：** auth 9010 / entry 9020 / stock 9030

---

## 一、通用返回体 `Result<T>`

| 字段 | 类型 | 说明 |
|------|------|------|
| resultCode | int | 0=成功，1=业务失败，-1=系统异常 |
| errorCode | String | 业务错误码（见下表） |
| errorInfo | String | 错误信息 |
| data | T | 返回数据 |

### 业务错误码

| 错误码 | 含义 |
|--------|------|
| 1001 | 借贷不平衡 |
| 1002 | 库存不足 |
| 1003 | 分录不存在 |
| 1004 | 会计科目不存在 |
| 1005 | 数据已被修改（乐观锁冲突） |
| 88888 | 系统异常 |

---

## 二、认证服务（auth，:9010）

### 2.1 登录

- **POST `/login`**（表单登录）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | form | 是 | 用户名 |
| password | form | 是 | 密码 |

响应（登录成功）：

```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "username": "admin",
    "token": "eyJhbGciOi...",
    "roles": ["USER_ADD", "USER_SELECT"]
  }
}
```

后续请求携带 `Authorization: Bearer <token>`。

### 2.2 用户管理（`/user`）

| 接口 | 方法 | 参数 | 返回 |
|------|------|------|------|
| `/user/getList` | GET | queryParams（查询条件） | Result\<List\<HakiraUser\>\> |
| `/user/register` | POST | body: HakiraUser | Result |
| `/user/registerByForm` | POST | username, password, email | Result |

---

## 三、分录服务（entry，:9020）

### 3.1 录入分录

- **POST `/entry/post`**

请求体 `JournalEntryRequest`：

```json
{
  "voucherNo": "PZ-001",
  "entryDate": "2026-08-14",
  "description": "采购原材料",
  "entries": [
    { "accountCode": "1403", "accountName": "原材料", "debitAmount": 1000, "creditAmount": 0 },
    { "accountCode": "1002", "accountName": "银行存款", "debitAmount": 0, "creditAmount": 1000 }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| voucherNo | String | 凭证号 |
| entryDate | String | 记账日期（yyyy-MM-dd） |
| description | String | 摘要 |
| entries[].accountCode | String | 科目编码（须在会计科目表存在） |
| entries[].accountName | String | 科目名称 |
| entries[].debitAmount | BigDecimal | 借方金额 |
| entries[].creditAmount | BigDecimal | 贷方金额 |

响应 `JournalEntryResponse`（借贷平衡校验通过，status=POSTED）：

```json
{
  "entryId": "20260814113147907110",
  "voucherNo": "PZ-001",
  "entryDate": "2026-08-14",
  "description": "采购原材料",
  "totalDebit": 1000,
  "totalCredit": 1000,
  "status": "POSTED",
  "entries": [
    { "accountCode": "1403", "accountName": "原材料", "debitAmount": 1000, "creditAmount": 0 },
    { "accountCode": "1002", "accountName": "银行存款", "debitAmount": 0, "creditAmount": 1000 }
  ]
}
```

> 借贷不平衡 → 错误码 1001；科目不存在 → 1004。

### 3.2 查询分录

- **GET `/entry/{entryId}`**

| 参数 | 说明 |
|------|------|
| entryId | 分录ID（20位雪花ID） |

返回 `JournalEntryResponse`；不存在 → 1003。

### 3.3 搜索分录

- **POST `/entry/search`**

请求体 `EntrySearchRequest`：

| 字段 | 类型 | 说明 |
|------|------|------|
| fromDate | String | 起始日期（yyyy-MM-dd，可选） |
| toDate | String | 结束日期（可选） |
| accountCode | String | 科目编码（可选） |
| status | String | 状态（可选） |

返回 `List<JournalEntryResponse>`。

---

## 四、库存服务（stock，:9030）

### 4.1 入库

- **POST `/stock/inbound`**

请求体 `StockMovementRequest`：

```json
{ "itemCode": "ITEM-001", "itemName": "原材料A", "quantity": 100, "unit": "件" }
```

| 字段 | 类型 | 说明 |
|------|------|------|
| itemCode | String | 物资编码 |
| itemName | String | 物资名称 |
| quantity | BigDecimal | 数量 |
| unit | String | 单位 |
| relatedVoucherNo | String | 关联凭证号（可选） |
| remark | String | 备注（可选） |

响应 `StockMovementResponse`（direction=INBOUND）。

### 4.2 出库

- **POST `/stock/outbound`**

请求体同 `StockMovementRequest`。响应 direction=OUTBOUND。

> 库存不足 → 错误码 1002。

### 4.3 库存快照

- **GET `/stock/snapshot/{itemCode}`**

响应 `StockSnapshotResponse`：

```json
{
  "itemCode": "ITEM-001",
  "itemName": "原材料A",
  "currentQuantity": 100,
  "unit": "件",
  "lastUpdateTime": "2026-08-14 11:31:58"
}
```

### 4.4 库存流水

- **GET `/stock/movements/{itemCode}`**

| 参数 | 说明 |
|------|------|
| itemCode | 物资编码 |
| fromDate | 起始日期（可选） |
| toDate | 结束日期（可选） |

返回 `List<StockMovementResponse>`（按时间倒序）。

---

## 五、审批流服务（workflow）

### 5.1 发起审批

- **POST `/workflow/approval/start`**

请求体：`Map<String, Object>`（业务参数）。返回流程实例信息。

### 5.2 查询待办任务

- **GET `/workflow/approval/tasks`**

返回 `List<Map<String, Object>>`。

### 5.3 完成任务

- **POST `/workflow/approval/complete/{taskId}`**

| 参数 | 说明 |
|------|------|
| taskId | 任务ID（路径参数） |

返回完成结果字符串。

---

## 六、批处理服务（task）

### 6.1 触发对账

- **POST `/task/reconciliation/run`**

无参数，返回对账结果 `Map<String, Object>`。

---

## 七、鉴权说明

除 `/login`、`/oauthGithub` 外，其余接口需携带 JWT：

```
Authorization: Bearer <token>
```

无令牌或令牌失效 → 网关返回 401。
