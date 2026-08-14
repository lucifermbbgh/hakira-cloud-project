package com.hakira.ledger.stock.mapper;

import com.hakira.ledger.stock.pojo.StockMovement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存流水表 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface StockMovementMapper {

    @Insert("INSERT INTO stock_movement(movement_id, item_code, item_name, direction, quantity, unit, related_voucher_no, movement_date, remark, status, version) " +
            "VALUES(#{movementId}, #{itemCode}, #{itemName}, #{direction}, #{quantity}, #{unit}, #{relatedVoucherNo}, #{movementDate}, #{remark}, 'ACTIVE', 0)")
    int insert(StockMovement movement);

    @Select("<script>" +
            "SELECT * FROM stock_movement m WHERE 1=1" +
            "<if test='itemCode != null and itemCode != \"\"'> AND m.item_code = #{itemCode}</if>" +
            "<if test='fromDate != null and fromDate != \"\"'> AND m.movement_date &gt;= #{fromDate}</if>" +
            "<if test='toDate != null and toDate != \"\"'> AND m.movement_date &lt;= #{toDate}</if>" +
            " ORDER BY m.movement_date DESC, m.movement_id DESC" +
            "</script>")
    List<StockMovement> search(@Param("itemCode") String itemCode,
                               @Param("fromDate") String fromDate,
                               @Param("toDate") String toDate);
}
