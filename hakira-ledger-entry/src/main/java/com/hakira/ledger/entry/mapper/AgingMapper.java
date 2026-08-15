package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.AgingRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 往来账龄 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface AgingMapper {

    /** 往来单位列表（维度值） */
    @Select("SELECT value_code AS valueCode, value_name AS valueName " +
            "FROM auxiliary_value WHERE dimension_code = #{dimension} AND status = 'ACTIVE' ORDER BY value_code")
    List<AgingRow> selectPartners(@Param("dimension") String dimension);

    /** 应收/应付明细：按往来单位 + 发生日期聚合（JOIN 维度值取名称） */
    @Select("SELECT aux.value_code AS valueCode, MAX(v.value_name) AS valueName, l.entry_date AS entryDate, " +
            "SUM(l.debit_amount) AS debitAmount, SUM(l.credit_amount) AS creditAmount " +
            "FROM journal_entry_line l " +
            "JOIN journal_entry_line_aux aux ON l.line_id = aux.line_id AND aux.dimension_code = #{dimension} " +
            "JOIN auxiliary_value v ON v.dimension_code = aux.dimension_code AND v.value_code = aux.value_code " +
            "WHERE l.subject_code = #{subjectCode} AND l.entry_date <= #{asOfDate} " +
            "GROUP BY aux.value_code, l.entry_date ORDER BY aux.value_code, l.entry_date")
    List<AgingRow> selectAgingDetails(@Param("dimension") String dimension,
                                      @Param("subjectCode") String subjectCode,
                                      @Param("asOfDate") LocalDate asOfDate);
}
