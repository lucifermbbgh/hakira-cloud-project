package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.LedgerRow;
import com.hakira.ledger.entry.pojo.ReportItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务报表 Mapper
 * <p>
 * 资产负债表/利润表读余额物化（O(1) 不扫流水），现金流量表按 CASH_FLOW 维度聚合（带分区裁剪）。
 *
 * @author hakiraKafka
 */
@Mapper
public interface ReportMapper {

    /** 资产负债表：期间余额 + 科目分类（JOIN 科目字典） */
    @Select("SELECT ab.subject_code AS subjectCode, ab.subject_name AS subjectName, " +
            "s.category AS category, s.balance_direction AS balanceDirection, " +
            "ab.closing_debit AS closingDebit, ab.closing_credit AS closingCredit " +
            "FROM account_balance ab JOIN account_subject s ON ab.subject_code = s.subject_code " +
            "WHERE ab.period = #{period} ORDER BY ab.subject_code")
    List<ReportItem> selectBalanceByPeriod(@Param("period") String period);

    /** 利润表：损益类科目本期发生额（从流水聚合，排除结转凭证——结转会把损益归零） */
    @Select("SELECT l.subject_code AS subjectCode, s.subject_name AS subjectName, s.balance_direction AS balanceDirection, " +
            "SUM(l.debit_amount) AS periodDebit, SUM(l.credit_amount) AS periodCredit " +
            "FROM journal_entry_line l " +
            "JOIN account_subject s ON l.subject_code = s.subject_code " +
            "JOIN journal_entry e ON l.entry_id = e.entry_id AND l.entry_date = e.entry_date " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "AND s.category = '损益' AND e.description NOT LIKE '结转%' " +
            "GROUP BY l.subject_code, s.subject_name, s.balance_direction")
    List<ReportItem> selectProfitLossByPeriod(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /** 现金流量表：现金科目按 CASH_FLOW 维度聚合（流入=借，流出=贷） */
    @Select("SELECT aux.value_code AS valueCode, " +
            "SUM(l.debit_amount) AS periodDebit, SUM(l.credit_amount) AS periodCredit " +
            "FROM journal_entry_line l " +
            "JOIN journal_entry_line_aux aux ON l.line_id = aux.line_id AND aux.dimension_code = 'CASH_FLOW' " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "AND l.subject_code IN ('1001','1002','1012') " +
            "GROUP BY aux.value_code")
    List<ReportItem> selectCashFlowByPeriod(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /** 明细账：某科目某期间逐笔分录 */
    @Select("SELECT e.voucher_no AS voucherNo, e.entry_date AS entryDate, l.description AS description, " +
            "l.debit_amount AS debitAmount, l.credit_amount AS creditAmount " +
            "FROM journal_entry_line l " +
            "JOIN journal_entry e ON l.entry_id = e.entry_id AND l.entry_date = e.entry_date " +
            "WHERE l.subject_code = #{subjectCode} AND l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "ORDER BY l.entry_date, e.voucher_no, l.line_no")
    List<LedgerRow> selectDetailLedger(@Param("subjectCode") String subjectCode,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /** 日记账：某期间序时全部分录 */
    @Select("SELECT voucher_no AS voucherNo, entry_date AS entryDate, description, " +
            "total_debit AS debitAmount, total_credit AS creditAmount " +
            "FROM journal_entry WHERE entry_date >= #{startDate} AND entry_date < #{endDate} " +
            "ORDER BY entry_date, voucher_no")
    List<LedgerRow> selectJournal(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /** 总账明细：某期间所有分录行（按科目分组） */
    @Select("SELECT l.subject_code AS subjectCode, e.voucher_no AS voucherNo, e.entry_date AS entryDate, " +
            "l.description AS description, l.debit_amount AS debitAmount, l.credit_amount AS creditAmount " +
            "FROM journal_entry_line l " +
            "JOIN journal_entry e ON l.entry_id = e.entry_id AND l.entry_date = e.entry_date " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "ORDER BY l.subject_code, l.entry_date, e.voucher_no, l.line_no")
    List<LedgerRow> selectLedgerDetails(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}
