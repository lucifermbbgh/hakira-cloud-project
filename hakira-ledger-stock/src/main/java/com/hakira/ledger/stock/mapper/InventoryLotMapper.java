package com.hakira.ledger.stock.mapper;

import com.hakira.ledger.stock.pojo.InventoryLot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存批次 Mapper（FIFO 计价）
 *
 * @author hakiraKafka
 */
@Mapper
public interface InventoryLotMapper {

    @Insert("INSERT INTO inventory_lot(item_code, unit_cost, remaining_quantity, inbound_date, status) " +
            "VALUES(#{itemCode}, #{unitCost}, #{remainingQuantity}, #{inboundDate}, 'ACTIVE')")
    int insert(InventoryLot lot);

    /** 查有效批次（按入库日期升序，FIFO 扣减顺序） */
    @Select("SELECT * FROM inventory_lot WHERE item_code = #{itemCode} AND status = 'ACTIVE' " +
            "AND remaining_quantity > 0 ORDER BY inbound_date, lot_id")
    List<InventoryLot> selectActiveLots(@Param("itemCode") String itemCode);

    @Update("UPDATE inventory_lot SET remaining_quantity = #{remainingQuantity}, " +
            "status = CASE WHEN #{remainingQuantity} <= 0 THEN 'EXHAUSTED' ELSE 'ACTIVE' END " +
            "WHERE lot_id = #{lotId}")
    int updateRemaining(@Param("lotId") Long lotId, @Param("remainingQuantity") BigDecimal remainingQuantity);
}
