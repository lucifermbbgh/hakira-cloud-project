# hakira-cloud-project — Phase 3 审批流与批处理详细设计

> **阶段：** Phase 3 · 审批流与批处理
> **状态：** ✅ 已完成

---

## 一、模块分层

| 模块 | 文件 | 职责 |
|------|------|------|
| 审批流 | `workflow/controller/ApprovalController.java` | `/workflow/approval` 接口 |
| 批处理 | `task/controller/ReconciliationController.java` | `/task/reconciliation` 接口 |
| 调度 | task 模块 Quartz 配置 | 定时任务调度 |

## 二、审批流设计

### 2.1 流程模型

```
发起审批（startApproval）
    │  传入业务参数 params
    ▼
创建 Flowable 流程实例
    │
    ▼
生成待办任务（getPendingTasks）
    │  查询当前用户的待办任务列表
    ▼
完成任务（completeTask）
    │  传入审批结果
    ▼
流程流转到下一节点 / 结束
```

### 2.2 接口定义

```java
// 发起审批
POST /workflow/approval/start
    params: Map<String, Object>   // 业务参数
    return: Map<String, Object>   // 流程实例信息

// 查询待办任务
GET /workflow/approval/tasks
    return: List<Map<String, Object>>

// 完成任务
POST /workflow/approval/complete/{taskId}
    return: String               // 完成结果
```

## 三、批处理设计

### 3.1 对账任务

```
POST /task/reconciliation/run
    │
    ▼
触发对账 Job（Quartz 调度 / 手动触发）
    │
    ▼
Spring Batch 批量处理（读取分录/库存数据，核对一致性）
    │
    ▼
返回对账结果 Map
```

### 3.2 调度机制

| 机制 | 用途 |
|------|------|
| Quartz | 定时触发对账任务（如每日凌晨） |
| Spring Batch | 批量数据读写、分片处理 |
| 手动触发 | `/task/reconciliation/run` 便于调试 |

## 四、关键实现细节

| 细节 | 说明 |
|------|------|
| Flowable | 流程引擎负责流程定义与实例管理 |
| 任务查询 | 按用户/角色查询待办任务 |
| Batch | 对账任务读分录流水，校验借贷平衡与库存一致性 |

> 注：Phase 3 为框架搭建，具体审批流程定义和对账规则在后续业务阶段细化。
