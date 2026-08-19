# hakira-cloud-project — Phase 3 审批流与批处理方案设计

> **阶段：** Phase 3 · 审批流与批处理（Flowable + Quartz + Spring Batch）
> **状态：** ✅ 已完成
> **关联提交：** c66c50a2

---

## 一、阶段目标

在会计核心业务基础上，引入**工作流审批**与**定时批处理**能力：凭证/业务经审批流流转，定期批量对账，形成「业务 → 审批 → 批处理」的闭环。

## 二、阶段范围

| 服务 | 模块 | 职责 |
|------|------|------|
| 审批流服务 | `hakira-ledger-workflow` | Flowable 工作流引擎，审批流程 |
| 批处理服务 | `hakira-ledger-task` | Quartz 定时调度 + Spring Batch 批量对账 |

## 三、设计要点

### 3.1 审批流设计（Flowable）

| 决策 | 方案 |
|------|------|
| 引擎 | Flowable 工作流引擎 |
| 流程 | 发起审批 → 待办任务 → 完成任务 |
| 接口 | `/workflow/approval/start`、`/tasks`、`/complete/{taskId}` |

### 3.2 批处理设计（Quartz + Batch）

| 决策 | 方案 |
|------|------|
| 调度 | Quartz 定时触发 |
| 批处理 | Spring Batch 批量对账 |
| 模块 | task 单模块承载（不拆分） |
| 接口 | `/task/reconciliation/run`（手动触发对账） |

## 四、业务接口设计

### 4.1 审批流（`/workflow/approval`）

| 接口 | 方法 | 功能 |
|------|------|------|
| `/start` | POST | 发起审批流程 |
| `/tasks` | GET | 查询待办任务 |
| `/complete/{taskId}` | POST | 完成指定任务 |

### 4.2 批处理（`/task/reconciliation`）

| 接口 | 方法 | 功能 |
|------|------|------|
| `/run` | POST | 手动触发对账任务 |

## 五、阶段验收标准

- [x] 审批流发起/查询/完成可用
- [x] 对账任务可手动触发
- [x] Quartz 定时调度 + Batch 批处理集成
