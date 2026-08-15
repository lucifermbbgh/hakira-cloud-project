package com.hakira.ledger.stock.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出库成本结转 Mapper（直接写 journal_entry，与 entry 服务同库）
 *
 * @author hakiraKafka
 */
@Mapper
public interface CostTransferMapper {

    @Select("SELECT MAX(voucher_no) FROM journal_entry WHERE voucher_no LIKE CONCAT('PZ-', #{dateStr}, '-%')")
    String selectMaxVoucherNo(@Param("dateStr") String dateStr);

    @Insert("INSERT INTO journal_entry(entry_id, entry_date, voucher_no, description, total_debit, total_credit, status, version) " +
            "VALUES(#{entryId}, #{entryDate}, #{voucherNo}, #{description}, #{total}, #{total}, 'POSTED', 0)")
    int insertEntry(@Param("entryId") String entryId,
                    @Param("entryDate") LocalDate entryDate,
                    @Param("voucherNo") String voucherNo,
                    @Param("description") String description,
                    @Param("total") BigDecimal total);

    @Insert("INSERT INTO journal_entry_line(entry_id, entry_date, line_no, subject_code, subject_name, description, debit_amount, credit_amount) " +
            "VALUES(#{entryId}, #{entryDate}, #{lineNo}, #{subjectCode}, #{subjectName}, #{description}, #{debit}, #{credit})")
    int insertLine(@Param("entryId") String entryId,
                   @Param("entryDate") LocalDate entryDate,
                   @Param("lineNo") int lineNo,
                   @Param("subjectCode") String subjectCode,
                   @Param("subjectName") String subjectName,
                   @Param("description") String description,
                   @Param("debit") BigDecimal debit,
                   @Param("credit") BigDecimal credit);
}
