# hakira-cloud-project — Phase 7 会计科目与辅助核算体系详细设计

> **阶段：** Phase 7 · 会计科目与辅助核算体系
> **状态：** ✅ 已完成

---

## 一、表结构设计

### 1.1 account_subject 扩展（层级）

| 新增字段 | 类型 | 说明 |
|---------|------|------|
| parent_code | varchar(20) | 上级科目编码（NULL=一级科目） |
| subject_level | tinyint | 科目级次（1=一级，2=二级…） |

完整字段：`subject_code`(PK) + `subject_name` + `category`(资产/负债/权益/成本/损益) + `balance_direction`(D借方/C贷方) + `parent_code` + `subject_level` + `status` + `version` + 审计字段。

### 1.2 完整科目体系（78 一级 + 4 明细）

| 类别 | 一级科目数 | 余额方向 | 明细示例 |
|------|-----------|---------|---------|
| 资产 | 37 | D（备抵类 C） | 100201 银行存款-工商银行 |
| 负债 | 16 | C | — |
| 权益 | 6 | C（库存股 D） | — |
| 成本 | 4 | D | — |
| 损益 | 15 | 收入 C / 费用 D | 222101 应交税费-应交增值税 |

### 1.3 辅助核算三张表

| 表 | 主键 | 说明 |
|----|------|------|
| auxiliary_dimension | dimension_code | 维度字典（DEPT/PROJECT/CUSTOMER/SUPPLIER/CASH_FLOW） |
| auxiliary_value | value_id（自增）+ uk(dimension_code, value_code) | 维度值 |
| journal_entry_line_aux | (line_id, dimension_code) | 分录行挂多维度 |

## 二、辅助核算维度设计

| 维度编码 | 维度名称 | 示例值 |
|---------|---------|--------|
| DEPT | 部门 | D001技术部 / D002财务部 / D003销售部 |
| PROJECT | 项目 | P001研发项目A / P002工程项目B |
| CUSTOMER | 客户 | C001客户甲 / C002客户乙 |
| SUPPLIER | 供应商 | S001供应商丙 / S002供应商丁 |
| CASH_FLOW | 现金流量 | CF001经营 / CF002投资 / CF003筹资 |

## 三、代码改造

### 3.1 DTO（JournalEntryRequest.EntryLine）

```java
public static class EntryLine {
    private String accountCode;
    private String accountName;
    private String description;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private List<AuxEntry> aux;   // 新增：辅助核算维度列表（可选）

    public static class AuxEntry {
        private String dimensionCode;  // 维度编码
        private String valueCode;      // 维度值编码
    }
}
```

### 3.2 Mapper 改造

| 文件 | 改动 |
|------|------|
| JournalEntryLineMapper | insert 加 `@Options(useGeneratedKeys=true, keyProperty="lineId")` 回填自增行ID |
| JournalEntryLineAuxMapper（新增） | 写 journal_entry_line_aux 关联 |
| AuxiliaryMapper（新增） | 校验维度/维度值合法性（countActiveDimension / countActiveValue） |

### 3.3 Service 改造（EntryServiceImpl）

写分录行后调用 `writeAux(line.getLineId(), e.getAux())`：

```
for each aux：
  校验维度存在（否则抛 1006）
  校验维度值存在（否则抛 1007）
  写 journal_entry_line_aux 关联
```

### 3.4 错误码扩展

| 错误码 | 含义 |
|--------|------|
| 1006 | AUX_DIMENSION_NOT_FOUND 辅助核算维度不存在 |
| 1007 | AUX_VALUE_NOT_FOUND 辅助核算维度值不存在 |

## 四、SQL 文件

`file/phase7_subject_aux.sql`（幂等，可重复执行）：扩展科目表 + 预置 78 一级科目 + 4 明细 + 建 3 张辅助核算表 + 预置维度/值。
