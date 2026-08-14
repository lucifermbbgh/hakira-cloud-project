package com.hakira.ledger.entry.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
