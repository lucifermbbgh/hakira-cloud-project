package com.hakira.ledger.api.dto.aging;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账龄项（单往来单位的各账龄段余额）
 */
@Data
public class AgingItem {
    private String partnerCode;
    private String partnerName;
    /** 总余额 */
    private BigDecimal totalBalance;
    /** 30 天内 */
    private BigDecimal aging30;
    /** 30-60 天 */
    private BigDecimal aging60;
    /** 60-90 天 */
    private BigDecimal aging90;
    /** 90 天以上（预警） */
    private BigDecimal agingOver90;
    /** 是否超龄（90 天以上有余额） */
    private boolean overdue;
}
