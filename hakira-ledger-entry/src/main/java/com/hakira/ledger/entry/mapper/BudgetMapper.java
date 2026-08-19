package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.Budget;
import com.hakira.ledger.entry.pojo.ReportItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 预算管理 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface BudgetMapper {

    /** 编制/更新预算（upsert + 乐观锁 version+1） */
    @Insert("INSERT INTO budget(period, subject_code, budget_amount, version) " +
            "VALUES(#{period}, #{subjectCode}, #{budgetAmount}, 0) " +
            "ON DUPLICATE KEY UPDATE budget_amount = VALUES(budget_amount), version = version + 1")
    int upsert(Budget budget);

    /** 查询期间预算 */
    @Select("SELECT * FROM budget WHERE period = #{period} ORDER BY subject_code")
    List<Budget> selectByPeriod(@Param("period") String period);

    /** 聚合实际发生额（按科目，含方向） */
    @Select("SELECT l.subject_code AS subjectCode, s.subject_name AS subjectName, s.balance_direction AS balanceDirection, " +
            "SUM(l.debit_amount) AS periodDebit, SUM(l.credit_amount) AS periodCredit " +
            "FROM journal_entry_line l JOIN account_subject s ON l.subject_code = s.subject_code " +
            "WHERE l.entry_date >= #{startDate} AND l.entry_date < #{endDate} " +
            "GROUP BY l.subject_code, s.subject_name, s.balance_direction")
    List<ReportItem> selectActualByPeriod(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
