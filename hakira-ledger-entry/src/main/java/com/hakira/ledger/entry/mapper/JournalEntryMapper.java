package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.JournalEntry;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 会计分录头表 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface JournalEntryMapper {

    @Insert("INSERT INTO journal_entry(entry_id, entry_date, voucher_no, description, total_debit, total_credit, status, version) " +
            "VALUES(#{entryId}, #{entryDate}, #{voucherNo}, #{description}, #{totalDebit}, #{totalCredit}, #{status}, 0)")
    int insert(JournalEntry entry);

    @Select("SELECT * FROM journal_entry WHERE entry_id = #{entryId}")
    JournalEntry selectByEntryId(@Param("entryId") String entryId);

    @Select("<script>" +
            "SELECT * FROM journal_entry e WHERE 1=1" +
            "<if test='fromDate != null and fromDate != \"\"'> AND e.entry_date &gt;= #{fromDate}</if>" +
            "<if test='toDate != null and toDate != \"\"'> AND e.entry_date &lt;= #{toDate}</if>" +
            "<if test='status != null and status != \"\"'> AND e.status = #{status}</if>" +
            "<if test='accountCode != null and accountCode != \"\"'> AND EXISTS (SELECT 1 FROM journal_entry_line l WHERE l.entry_id = e.entry_id AND l.entry_date = e.entry_date AND l.subject_code = #{accountCode})</if>" +
            " ORDER BY e.entry_date DESC, e.entry_id DESC" +
            "</script>")
    List<JournalEntry> search(@Param("fromDate") String fromDate,
                              @Param("toDate") String toDate,
                              @Param("accountCode") String accountCode,
                              @Param("status") String status);
}
