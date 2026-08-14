package com.hakira.ledger.entry.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 辅助核算维度/维度值校验 Mapper（Phase 7）
 *
 * @author hakiraKafka
 */
@Mapper
public interface AuxiliaryMapper {

    @Select("SELECT COUNT(*) FROM auxiliary_dimension WHERE dimension_code = #{dimensionCode} AND status = 'ACTIVE'")
    int countActiveDimension(@Param("dimensionCode") String dimensionCode);

    @Select("SELECT COUNT(*) FROM auxiliary_value WHERE dimension_code = #{dimensionCode} AND value_code = #{valueCode} AND status = 'ACTIVE'")
    int countActiveValue(@Param("dimensionCode") String dimensionCode, @Param("valueCode") String valueCode);
}
