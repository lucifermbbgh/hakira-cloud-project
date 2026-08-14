package com.hakira.ledger.entry.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会计科目表 Mapper（校验分录科目合法性）
 *
 * @author hakiraKafka
 */
@Mapper
public interface AccountSubjectMapper {

    @Select("SELECT COUNT(*) FROM account_subject WHERE subject_code = #{subjectCode} AND status = 'ACTIVE'")
    int countActiveByCode(@Param("subjectCode") String subjectCode);
}
