package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据追溯中的库存流水项（跨表查询 stock_movement）
 *
 * @author hakiraKafka
 */
@Data
public class AuditMovement {
    private String movementId;
    private String direction;
    private BigDecimal quantity;
    private String itemCode;
    private String itemName;
}
