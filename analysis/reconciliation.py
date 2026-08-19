#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Spark 批式对账作业（Phase 6）
功能：读 MySQL journal_entry 表，校验借贷平衡（total_debit == total_credit），
     输出总分录数 / 平衡分录数 / 不平衡分录数，列出不平衡分录明细。

提交方式（在 Spark Master 容器内）：
    spark-submit --master spark://spark-master:7077 \
        --jars mysql-connector-j-8.1.0.jar reconciliation.py

依赖：mysql-connector-j（需 --jars 加载）；必须显式指定 driver 类，
      否则报 "No suitable driver"。
"""
from pyspark.sql import SparkSession

spark = SparkSession.builder \
    .appName("LedgerReconciliation") \
    .getOrCreate()

# 读 MySQL journal_entry 表（容器网络内用容器名 hakira_ledger_mysql）
df = spark.read \
    .format("jdbc") \
    .option("url", "jdbc:mysql://hakira_ledger_mysql:3306/hakira_ledger") \
    .option("dbtable", "journal_entry") \
    .option("driver", "com.mysql.cj.jdbc.Driver") \
    .option("user", "root") \
    .option("password", "root") \
    .load()

total = df.count()
unbalanced = df.filter("total_debit != total_credit")

print("=" * 50)
print("        借贷平衡对账报告（Spark）")
print("=" * 50)
print(f"总分录数   : {total}")
print(f"平衡分录数 : {total - unbalanced.count()}")
print(f"不平衡分录 : {unbalanced.count()}")

if unbalanced.count() > 0:
    print("-" * 50)
    print("不平衡分录明细：")
    unbalanced.select("entry_id", "voucher_no",
                      "total_debit", "total_credit", "status").show(truncate=False)

spark.stop()
