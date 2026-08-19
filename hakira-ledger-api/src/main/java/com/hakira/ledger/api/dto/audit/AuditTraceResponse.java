package com.hakira.ledger.api.dto.audit;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据全链路追溯结果（凭证 → 分录 → 辅助核算 → 库存流水）
 */
@Data
public class AuditTraceResponse {
    private String entryId;
    private String voucherNo;
    private String entryDate;
    private String description;
    private String status;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    /** 分录行 */
    private List<Line> lines;
    /** 关联库存流水 */
    private List<Movement> movements;

    @Data
    public static class Line {
        private Long lineId;
        private String subjectCode;
        private String subjectName;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private List<Aux> aux;
    }

    @Data
    public static class Aux {
        private String dimensionCode;
        private String valueCode;
    }

    @Data
    public static class Movement {
        private String movementId;
        private String direction;
        private BigDecimal quantity;
        private String itemCode;
        private String itemName;
    }
}
