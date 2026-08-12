package com.hakira.ledger.api.dto.stock;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockMovementResponse {
    private String movementId;
    private String itemCode;
    private String itemName;
    private String direction;       // INBOUND/OUTBOUND
    private BigDecimal quantity;
    private String unit;
    private String relatedVoucherNo;
    private String movementDate;
    private String remark;
}
