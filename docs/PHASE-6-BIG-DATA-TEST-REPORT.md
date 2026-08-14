# hakira-cloud-project — Phase 6 大数据分析层测试报告

> **阶段：** Phase 6 · 大数据分析层（Flink + Spark）
> **状态：** ✅ 已通过

---

## 一、测试范围

| 测试项 | 内容 |
|--------|------|
| 集群搭建 | Flink(1JM+1TM) + Spark(1Master+1Worker) docker 集群 |
| 节点注册 | TaskManager/Worker 注册到 JobManager/Master |
| 作业提交 | Spark 批式 + Flink 流式 |
| 自定义分析 | Spark 对账（读 MySQL）+ Flink 流式统计 |

## 二、测试环境

| 项目 | 值 |
|------|-----|
| 系统 | Ubuntu VM（Linux 本地测试） |
| Flink | flink:1.18.0-scala_2.12（Docker 集群） |
| Spark | apache/spark:3.5.0（Docker 集群） |
| 数据源 | MySQL 8.0（hakira_ledger 库） |

## 三、测试用例与结果

### 3.1 集群运行验证（8 项全通过）

| # | 用例 | 预期 | 结果 |
|---|------|------|------|
| 1 | 4 个大数据容器 running | 全部 Up | ✅ |
| 2 | Flink UI 8081 | HTTP 200 | ✅ |
| 3 | Flink TaskManager 注册 | 1 个 / 2 slots | ✅ |
| 4 | Spark UI 8080 | HTTP 200 | ✅ |
| 5 | Spark Worker 注册 | 1 个 / 2 cores | ✅ |
| 6 | Spark SparkPi 作业 | Pi≈3.14 | ✅ |
| 7 | Flink WordCount 作业 | FINISHED，词频正确 | ✅ |
| 8 | 端口 8081/8080/7077 | 监听 0.0.0.0 | ✅ |

### 3.2 自定义分析作业（2 项全通过）

| # | 用例 | 预期 | 结果 |
|---|------|------|------|
| 9 | Spark 对账（读 journal_entry 校验借贷平衡） | 2 条分录全平衡 | ✅ |
| 10 | Flink 流式统计（10 条流水事件） | 词频统计正确 | ✅ |

## 四、问题与处理（6 个）

| # | 问题 | 根因 | 处理方案 | 结果 |
|---|------|------|---------|------|
| 1 | Spark Invalid master URL / Flink leader election 503 | 下划线 hostname 导致 Java URI.getHost() 返回 null | hostname 改连字符 | ✅ |
| 2 | compose unexpected type map | FLINK_PROPERTIES 冒号被解析成 map | 单引号包裹 | ✅ |
| 3 | Flink 作业 FileNotFoundException | 输入文件在 JobManager，TaskManager 访问不到 | 跨节点共享文件/挂载卷 | ✅ |
| 4 | Spark No suitable driver | 未显式指定 MySQL 驱动类 | .option("driver", "com.mysql.cj.jdbc.Driver") | ✅ |
| 5 | socket 发送数据卡住 | nc 单连接 backlog，第二客户端阻塞 | python3 socket 服务器 | ✅ |
| 6 | bash /dev/tcp 发送卡住 | 连接不自动关闭 | 改用 python3 socket | ✅ |

## 五、结论

Phase 6 大数据分析层基建完成：Flink + Spark 集群搭建成功，Spark 批式对账 + Flink 流式统计两条分析链路端到端跑通，均读取 MySQL 真实会计数据。控制台（Flink UI 8081 / Spark UI 8080）可浏览器访问。

## 六、遗留

| 问题 | 说明 |
|------|------|
| yarn 集成 | 按用户指示搁置，standalone 模式足够 |
| 客户端 API 提交 | 后续用 flink/spark 服务客户端 API 替代手动提交 |
| Flink CDC | 进阶方案（需开 MySQL binlog），当前 JDBC 直连 |
