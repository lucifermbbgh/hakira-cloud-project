package com.hakira.ledger.api.dto.entry;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class JournalEntryRequest {
    /** 凭证号 */
    private String voucherNo;
    /** 记账日期 yyyy-MM-dd */
    private String entryDate;
    /** 摘要说明 */
    private String description;
    /** 分录行列表 */
    private List<EntryLine> entries;

    @Data
    public static class EntryLine {
        /** 会计科目编码 */
        private String accountCode;
        /** 会计科目名称 */
        private String accountName;
        /** 摘要 */
        private String description;
        /** 借方金额 */
        private BigDecimal debitAmount;
        /** 贷方金额 */
        private BigDecimal creditAmount;
    }
}
