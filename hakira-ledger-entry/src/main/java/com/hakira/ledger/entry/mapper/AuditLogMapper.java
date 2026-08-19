package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.AuditLog;
import com.hakira.ledger.entry.pojo.AuditMovement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审计日志 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_log(operation, operator, entity_type, entity_id, detail) " +
            "VALUES(#{operation}, #{operator}, #{entityType}, #{entityId}, #{detail})")
    int insert(AuditLog log);

    @Select("SELECT * FROM audit_log ORDER BY log_id DESC LIMIT #{limit}")
    List<AuditLog> selectRecent(@Param("limit") int limit);

    /** 数据追溯：按凭证号关联库存流水 */
    @Select("SELECT movement_id AS movementId, direction, quantity, item_code AS itemCode, item_name AS itemName " +
            "FROM stock_movement WHERE related_voucher_no = #{voucherNo} ORDER BY movement_id")
    List<AuditMovement> selectMovementsByVoucher(@Param("voucherNo") String voucherNo);
}
