package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.JournalEntryLineAux;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 分录行辅助核算关联表 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface JournalEntryLineAuxMapper {

    @Insert("INSERT INTO journal_entry_line_aux(line_id, dimension_code, value_code) " +
            "VALUES(#{lineId}, #{dimensionCode}, #{valueCode})")
    int insert(@Param("lineId") Long lineId,
               @Param("dimensionCode") String dimensionCode,
               @Param("valueCode") String valueCode);

    @Select("SELECT la.line_id AS lineId, la.dimension_code AS dimensionCode, la.value_code AS valueCode " +
            "FROM journal_entry_line_aux la " +
            "JOIN journal_entry_line l ON la.line_id = l.line_id " +
            "WHERE l.entry_id = #{entryId} AND l.entry_date = #{entryDate}")
    List<JournalEntryLineAux> selectByEntryId(@Param("entryId") String entryId,
                                              @Param("entryDate") LocalDate entryDate);
}
