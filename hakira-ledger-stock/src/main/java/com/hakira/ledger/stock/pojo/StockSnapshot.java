package com.hakira.ledger.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存快照实体（对应 stock_snapshot 表，状态表，每物资一行）
 *
 * @author hakiraKafka
 */
@Data
public class StockSnapshot {
    /** 物资编码 */
    private String itemCode;
    /** 物资名称 */
    private String itemName;
    /** 当前库存量 */
    private BigDecimal currentQuantity;
    /** 库存总成本 */
    private BigDecimal totalCost;
    /** 加权平均单价 */
    private BigDecimal weightedAvgCost;
    /** 计价方法：WEIGHTED_AVG / FIFO */
    private String costingMethod;
    /** 单位 */
    private String unit;
    /** 状态：ACTIVE=有效 */
    private String status;
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
