package com.hakira.ledger.api.dto.stock;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockSnapshotResponse {
    private String itemCode;
    private String itemName;
    private BigDecimal currentQuantity;
    private String unit;
    private String lastUpdateTime;
}
