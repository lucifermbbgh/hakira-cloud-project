package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.JournalEntryLine;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 会计分录行表 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface JournalEntryLineMapper {

    @Insert("INSERT INTO journal_entry_line(entry_id, entry_date, line_no, subject_code, subject_name, description, debit_amount, credit_amount) " +
            "VALUES(#{entryId}, #{entryDate}, #{lineNo}, #{subjectCode}, #{subjectName}, #{description}, #{debitAmount}, #{creditAmount})")
    int insert(JournalEntryLine line);

    @Select("SELECT * FROM journal_entry_line WHERE entry_id = #{entryId} AND entry_date = #{entryDate} ORDER BY line_no")
    List<JournalEntryLine> selectByEntryId(@Param("entryId") String entryId, @Param("entryDate") LocalDate entryDate);
}
