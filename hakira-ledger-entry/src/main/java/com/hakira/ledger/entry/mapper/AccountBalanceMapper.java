package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.AccountBalance;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 科目余额物化表 Mapper
 * <p>
 * 聚合查询均带 entry_date 范围条件，命中按月 RANGE 分区裁剪（Phase 9 高并发考量）。
 *
 * @author hakiraKafka
 */
@Mapper
public interface AccountBalanceMapper {

    /** 聚合分录行按科目（本期发生额，数据库端 GROUP BY 聚合） */
    @Select("SELECT l.subject_code AS subjectCode, s.subject_name AS subjectName, " +
            "SUM(l.debit_amount) AS periodDebit, SUM(l.credit_amount) AS periodCredit " +
            "FROM journal_entry_line l LEFT JOIN account_subject s ON l.subject_code = s.subject_code " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "GROUP BY l.subject_code, s.subject_name")
    List<AccountBalance> aggregateByPeriod(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /** 聚合损益类科目本期发生额（含余额方向，供损益结转） */
    @Select("SELECT l.subject_code AS subjectCode, s.subject_name AS subjectName, s.balance_direction AS balanceDirection, " +
            "SUM(l.debit_amount) AS periodDebit, SUM(l.credit_amount) AS periodCredit " +
            "FROM journal_entry_line l JOIN account_subject s ON l.subject_code = s.subject_code " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} AND s.category = '损益' " +
            "GROUP BY l.subject_code, s.subject_name, s.balance_direction")
    List<AccountBalance> aggregateProfitLoss(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /** 读期间余额物化值 */
    @Select("SELECT * FROM account_balance WHERE period = #{period} ORDER BY subject_code")
    List<AccountBalance> selectByPeriod(@Param("period") String period);

    /** 写入余额物化 */
    @Insert("INSERT INTO account_balance(period, subject_code, subject_name, opening_debit, opening_credit, " +
            "period_debit, period_credit, closing_debit, closing_credit, version) " +
            "VALUES(#{period}, #{subjectCode}, #{subjectName}, #{openingDebit}, #{openingCredit}, " +
            "#{periodDebit}, #{periodCredit}, #{closingDebit}, #{closingCredit}, 0)")
    int insertBalance(AccountBalance balance);

    /** 删除期间余额（重结账前清理） */
    @Delete("DELETE FROM account_balance WHERE period = #{period}")
    int deleteByPeriod(@Param("period") String period);

    /** 统计期间内已生成的结转凭证数（幂等检查：>0 则已结转） */
    @Select("SELECT COUNT(*) FROM journal_entry WHERE description LIKE '结转%' " +
            "AND entry_date >= #{startDate} AND entry_date < #{endDate}")
    int countTransferEntries(@Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);
}
