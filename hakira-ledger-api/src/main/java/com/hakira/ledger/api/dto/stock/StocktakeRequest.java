package com.hakira.ledger.api.dto.stock;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 存货盘点请求
 */
@Data
public class StocktakeRequest {
    /** 物资编码 */
    private String itemCode;
    /** 物资名称 */
    private String itemName;
    /** 实际盘点数量 */
    private BigDecimal actualQuantity;
    /** 单位 */
    private String unit;
    /** 备注 */
    private String remark;
}
