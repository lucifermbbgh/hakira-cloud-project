# hakira-cloud-project — Phase 6 大数据分析层详细设计

> **阶段：** Phase 6 · 大数据分析层（Flink + Spark）
> **状态：** 🚧 实施中（集群已搭建，验证通过）

---

## 一、集群架构

### 1.1 集群拓扑

| 节点 | 容器名 | hostname | 端口 | 资源 |
|------|--------|----------|------|------|
| Flink JobManager | hakira_ledger_flink_jobmanager | flink-jobmanager | UI 8081 | 1024m |
| Flink TaskManager | hakira_ledger_flink_taskmanager | flink-taskmanager | — | 2 slots / 2 cores |
| Spark Master | hakira_ledger_spark_master | spark-master | UI 8080 / RPC 7077 | — |
| Spark Worker | hakira_ledger_spark_worker | spark-worker | — | 2g / 2 cores |

### 1.2 镜像选型

| 集群 | 镜像 | 说明 |
|------|------|------|
| Flink | `flink:1.18.0-scala_2.12` | Apache 官方镜像 |
| Spark | `apache/spark:3.5.0` | Apache 官方镜像 |

### 1.3 资源评估结论

VM 25GiB 空闲内存 + 4 核，同时跑 Flink + Spark 集群，资源占用约 5-7GB，**充足**。

## 二、部署文件（file/docker-compose-bigdata.yml）

```yaml
services:
  hakira_ledger_flink_jobmanager:
    image: flink:1.18.0-scala_2.12
    hostname: flink-jobmanager          # 关键：连字符 hostname，避免 URI 解析失败
    ports: ["8081:8081"]
    command: jobmanager
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
      - 'FLINK_PROPERTIES=jobmanager.memory.process.size: 1024m'

  hakira_ledger_flink_taskmanager:
    image: flink:1.18.0-scala_2.12
    hostname: flink-taskmanager
    command: taskmanager
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
      - 'FLINK_PROPERTIES=taskmanager.numberOfTaskSlots: 2'

  hakira_ledger_spark_master:
    image: apache/spark:3.5.0
    hostname: spark-master
    ports: ["8080:8080", "7077:7077"]
    command: /opt/spark/bin/spark-class org.apache.spark.deploy.master.Master --host spark-master --port 7077 --webui-port 8080

  hakira_ledger_spark_worker:
    image: apache/spark:3.5.0
    hostname: spark-worker
    command: /opt/spark/bin/spark-class org.apache.spark.deploy.worker.Worker spark://spark-master:7077 --cores 2 --memory 2g
```

## 三、搭建过程（阶段实施内容）

### 步骤 1：编写 compose 文件

见上文 `file/docker-compose-bigdata.yml`，复用 `hakira_ledger_net` 网络（与 MySQL/Nacos 同网）。

### 步骤 2：拉取镜像并启动

```bash
docker compose -f file/docker-compose-bigdata.yml up -d
# 镜像 ~3.5GB，4 个容器全部 Started
```

### 步骤 3：验证节点注册

```bash
# Flink：UI 返回 taskmanagers=1, slots-total=2
curl http://127.0.0.1:8081/overview

# Spark：ALIVE, aliveworkers=1, cores=2, memory=2048
curl http://127.0.0.1:8080/json/
```

### 步骤 4：提交验证作业

```bash
# Spark：SparkPi 示例
docker exec hakira_ledger_spark_master /opt/spark/bin/spark-submit \
  --master spark://spark-master:7077 \
  --class org.apache.spark.examples.SparkPi \
  /opt/spark/examples/jars/spark-examples_2.12-3.5.0.jar 10
# → Pi is roughly 3.1426 ✅

# Flink：WordCount 示例（batch）
docker exec hakira_ledger_flink_taskmanager bash -c 'echo "hello world hello flink" > /tmp/input.txt'
docker exec hakira_ledger_flink_jobmanager flink run \
  /opt/flink/examples/batch/WordCount.jar --input /tmp/input.txt --output /tmp/output.txt
# → state=FINISHED, 词频统计正确 ✅
```

## 四、验证结果

| 验证项 | 结果 |
|--------|------|
| Flink JobManager + TaskManager 注册 | ✅ taskmanagers=1, slots=2 |
| Flink WordCount 作业 | ✅ FINISHED，词频正确 |
| Spark Master + Worker 注册 | ✅ aliveworkers=1, cores=2 |
| Spark SparkPi 作业 | ✅ Pi=3.1426 |

## 五、关键坑位与解决

### 5.1 下划线 hostname 导致 URI 解析失败（核心坑）

- **现象**：Spark Worker 报 `Invalid master URL: spark://hakira_ledger_spark_master:7077`；Flink 一直 leader election 503
- **根因**：Java `URI.getHost()` 对含下划线的 hostname 返回 `null`（下划线不是合法主机名字符）。实测 `spark://hakira_ledger_spark_master:7077` → host=null，`spark://spark-master:7077` → host=spark-master
- **解决**：容器 `hostname` 字段用**连字符**（flink-jobmaster / spark-master），容器名仍保持 `hakira_ledger_` 前缀（符合命名约束）

### 5.2 FLINK_PROPERTIES 冒号解析

- **现象**：compose 报 `unexpected type map[string]interface {}`
- **根因**：YAML 里 `FLINK_PROPERTIES=jobmanager.memory.process.size: 1024m` 的冒号被解析成嵌套 map
- **解决**：用单引号包裹 `'FLINK_PROPERTIES=jobmanager.memory.process.size: 1024m'`

### 5.3 跨节点文件访问

- **现象**：Flink 作业在 JobManager 创建输入文件，TaskManager 执行时报 `FileNotFoundException`
- **根因**：Flink 集群的作业在 TaskManager 执行，输入文件需在 TaskManager 可访问（分布式文件系统或挂载共享卷）
- **解决**：验证时在 TaskManager 也创建输入文件；生产需挂载共享存储或 HDFS

## 六、后续工作（下一小步）

1. 编写自定义 Spark 对账作业（读 MySQL journal_entry 校验借贷平衡）
2. 编写自定义 Flink 流式作业（实时流水统计）
3. flink/spark Spring Boot 服务通过客户端 API 提交作业到集群（替代手动提交）
