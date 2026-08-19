package com.hakira.ledger.api.dto.stock;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockMovementRequest {
    /** 物资编码 */
    private String itemCode;
    /** 物资名称 */
    private String itemName;
    /** 变动数量 */
    private BigDecimal quantity;
    /** 单价（入库传入） */
    private BigDecimal unitCost;
    /** 计价方法：WEIGHTED_AVG / FIFO（首次入库时设置） */
    private String costingMethod;
    /** 单位 */
    private String unit;
    /** 关联凭证号 */
    private String relatedVoucherNo;
    /** 备注 */
    private String remark;
}
