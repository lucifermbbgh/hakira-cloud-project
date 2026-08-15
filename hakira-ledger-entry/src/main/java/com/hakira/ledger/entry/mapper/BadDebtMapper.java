package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.ReportItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 坏账处理 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface BadDebtMapper {

    /** 1231 坏账准备余额（贷方=已计提） */
    @Select("SELECT SUM(debit_amount) AS periodDebit, SUM(credit_amount) AS periodCredit " +
            "FROM journal_entry_line WHERE subject_code = '1231' AND entry_date <= #{asOfDate}")
    ReportItem selectBadDebtBalance(@Param("asOfDate") LocalDate asOfDate);
}
