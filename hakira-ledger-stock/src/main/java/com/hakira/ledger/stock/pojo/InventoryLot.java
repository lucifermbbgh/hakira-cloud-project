package com.hakira.ledger.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存批次实体（对应 inventory_lot 表，FIFO 计价用）
 *
 * @author hakiraKafka
 */
@Data
public class InventoryLot {
    private Long lotId;
    private String itemCode;
    private BigDecimal unitCost;
    private BigDecimal remainingQuantity;
    private LocalDate inboundDate;
    private String status;
    private LocalDateTime createTime;
}
