package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.ReportItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 成本核算 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface CostMapper {

    /** 聚合 5001 生产成本按 COST_ITEM 维度（成本计算单） */
    @Select("SELECT aux.value_code AS valueCode, " +
            "SUM(l.debit_amount) AS periodDebit, SUM(l.credit_amount) AS periodCredit " +
            "FROM journal_entry_line l " +
            "JOIN journal_entry_line_aux aux ON l.line_id = aux.line_id AND aux.dimension_code = 'COST_ITEM' " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "AND l.subject_code = '5001' " +
            "GROUP BY aux.value_code")
    List<ReportItem> selectCostByPeriod(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /** 聚合 5101 制造费用本期发生额（分配用） */
    @Select("SELECT SUM(debit_amount) AS periodDebit, SUM(credit_amount) AS periodCredit " +
            "FROM journal_entry_line " +
            "WHERE entry_date >= #{startDate} AND entry_date < #{endDate} AND subject_code = '5101'")
    ReportItem selectOverheadByPeriod(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}
