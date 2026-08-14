# hakira-cloud-project — Phase 6 大数据分析层方案设计

> **阶段：** Phase 6 · 大数据分析层（Flink + Spark）
> **状态：** 🚧 实施中
> **定位：** 基建（分析能力底座），优先于业务深化

---

## 一、阶段目标

搭建 Flink + Spark **集群**（Docker 容器部署），让 `hakira-ledger-flink` / `hakira-ledger-spark` 两个模块具备「读 MySQL 会计流水 → 分析 → 输出」的分析能力，为后续报表/对账/监控提供基建。

## 二、现状

| 模块 | 现状 |
|------|------|
| hakira-ledger-flink | 空壳：flink-streaming-java + flink-clients 1.18.0 + 启动类 |
| hakira-ledger-spark | 空壳：spark-sql_2.12 + spark-core_2.12 3.5.0 + 启动类 |

## 三、资源评估与部署方式

### 3.1 资源评估

| 资源 | VM 可用 | Flink 集群 | Spark 集群 | 结论 |
|------|---------|-----------|-----------|------|
| 内存 | 25 GiB 空闲 | 2-4 GB | ~3 GB | 充足 |
| CPU | 4 核 | 1-2 核 | 1-2 核 | 充足 |
| 磁盘 | 56 GB | 镜像 ~1.5GB | 镜像 ~2GB | 充足 |

### 3.2 部署方式：Docker 容器（集群）

| 决策 | 方案 | 理由 |
|------|------|------|
| 部署方式 | Docker Compose 容器化集群 | 与 MySQL/Nacos 统一管理，容器名 `hakira_ledger` 前缀 |
| Flink | 1 JobManager + 1 TaskManager（`flink:1.18.0-scala_2.12` 官方） | 最小可用集群 |
| Spark | 1 Master + 1 Worker（`apache/spark:3.5.0` 官方） | 最小可用集群 |

### 3.3 集群拓扑

| 节点 | 容器名 | 端口 | 资源 |
|------|--------|------|------|
| Flink JobManager | hakira_ledger_flink_jobmanager | UI 8081 | 1024m |
| Flink TaskManager | hakira_ledger_flink_taskmanager | — | 1536m / 2 slots |
| Spark Master | hakira_ledger_spark_master | UI 8080 / RPC 7077 | — |
| Spark Worker | hakira_ledger_spark_worker | — | 2g / 2 cores |

### 3.4 作业提交方式（分阶段）

| 阶段 | 方式 |
|------|------|
| 基建验证（本阶段） | 手动提交：Flink Web UI 上传 jar / spark-submit 命令 |
| 后续集成 | flink/spark Spring Boot 服务用客户端 API 提交作业到集群 |

## 四、分析任务设计（基础版 3 个）

### 4.1 Spark 批式：借贷平衡对账（核心）

- 读 `journal_entry`，校验 `total_debit == total_credit`
- 找出不平衡的分录，输出对账报告
- 价值：验证会计数据一致性

### 4.2 Spark 批式：库存流水汇总

- 读 `stock_movement`，按 `item_code` 汇总入库/出库总量
- 输出各物资的净变动

### 4.3 Flink 流式：实时流水统计

- 从数据流（socket 演示）读取流水事件，实时统计
- 价值：验证流式处理链路

## 五、数据读取与输出

| 项 | 方案 |
|----|------|
| 数据读取 | JDBC 直连 MySQL（Flink CDC 留作进阶） |
| 结果输出 | 写回 MySQL 结果表 + 日志 |

## 六、关键风险与应对

| 风险 | 应对 |
|------|------|
| Flink/Spark 与 Spring Boot 3.2 依赖冲突（jackson/guava/slf4j） | 作业提交客户端模式下，Spring Boot 服务与集群解耦，冲突面减小 |
| 镜像体积大（~3.5GB） | 一次性拉取，后续复用 |
| 集群资源占用 | 已评估充足，容器限制内存 |

## 七、执行步骤

```
步骤1：写 docker-compose-bigdata.yml（Flink + Spark 集群）
步骤2：拉取镜像 + 启动集群
步骤3：验证集群运行（Web UI + 节点状态）
步骤4：提交 Spark 对账作业（读 MySQL 校验借贷平衡）
步骤5：提交 Flink 流式作业（演示）
步骤6：记录搭建过程到详细设计文档
```

## 八、阶段验收标准

- [x] Flink JobManager/TaskManager 正常运行
- [x] Spark Master/Worker 正常运行
- [x] Spark 对账作业跑通（能读 MySQL 校验平衡）
- [x] Flink 流式作业跑通
- [x] 搭建过程记录到详细设计文档
