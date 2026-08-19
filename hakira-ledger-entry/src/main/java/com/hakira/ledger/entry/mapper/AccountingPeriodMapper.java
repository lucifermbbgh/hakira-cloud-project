package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.AccountingPeriod;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 会计期间状态表 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface AccountingPeriodMapper {

    @Select("SELECT * FROM accounting_period WHERE period = #{period}")
    AccountingPeriod selectByPeriod(@Param("period") String period);

    @Insert("INSERT IGNORE INTO accounting_period(period, status, version) VALUES(#{period}, 'OPEN', 0)")
    int insertOpen(@Param("period") String period);

    @Update("UPDATE accounting_period SET status = #{status}, version = version + 1 " +
            "WHERE period = #{period} AND version = #{version}")
    int updateStatus(@Param("period") String period,
                     @Param("status") String status,
                     @Param("version") int version);
}
