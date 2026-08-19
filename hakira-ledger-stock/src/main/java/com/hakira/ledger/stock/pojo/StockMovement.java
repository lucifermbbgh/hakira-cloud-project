package com.hakira.ledger.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存流水实体（对应 stock_movement 表，按月分区）
 *
 * @author hakiraKafka
 */
@Data
public class StockMovement {
    /** 流水ID（20位雪花ID） */
    private String movementId;
    /** 物资编码 */
    private String itemCode;
    /** 物资名称 */
    private String itemName;
    /** 方向：INBOUND=入库 OUTBOUND=出库 */
    private String direction;
    /** 变动数量 */
    private BigDecimal quantity;
    /** 单价（入库传入/出库=加权平均单价） */
    private BigDecimal unitCost;
    /** 本次总成本 */
    private BigDecimal totalCost;
    /** 单位 */
    private String unit;
    /** 关联凭证号 */
    private String relatedVoucherNo;
    /** 变动日期（分区键） */
    private LocalDate movementDate;
    /** 备注 */
    private String remark;
    /** 状态：ACTIVE=有效 REVERSED=已冲销 */
    private String status;
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
