package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 固定资产卡片实体（对应 fixed_asset 表）
 *
 * @author hakiraKafka
 */
@Data
public class FixedAsset {
    /** 资产编码 */
    private String assetCode;
    /** 资产名称 */
    private String assetName;
    /** 资产类别 */
    private String category;
    /** 原值 */
    private BigDecimal originalValue;
    /** 残值率 */
    private BigDecimal residualRate;
    /** 折旧年限（月） */
    private Integer usefulLife;
    /** 折旧方法：STRAIGHT_LINE / DOUBLE_DECLINING */
    private String depreciationMethod;
    /** 累计折旧 */
    private BigDecimal accumulatedDepreciation;
    /** 净值 */
    private BigDecimal netValue;
    /** 状态：IN_USE / DISPOSED */
    private String status;
    /** 购置日期 */
    private LocalDate purchaseDate;
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
