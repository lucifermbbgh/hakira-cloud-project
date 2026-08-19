package com.hakira.ledger.stock.mapper;

import com.hakira.ledger.stock.pojo.StockSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 库存快照表 Mapper（状态表，入出库时乐观锁更新）
 *
 * @author hakiraKafka
 */
@Mapper
public interface StockSnapshotMapper {

    @Select("SELECT * FROM stock_snapshot WHERE item_code = #{itemCode}")
    StockSnapshot selectByItemCode(@Param("itemCode") String itemCode);

    @Insert("INSERT INTO stock_snapshot(item_code, item_name, current_quantity, total_cost, weighted_avg_cost, costing_method, unit, status, version) " +
            "VALUES(#{itemCode}, #{itemName}, #{currentQuantity}, #{totalCost}, #{weightedAvgCost}, #{costingMethod}, #{unit}, 'ACTIVE', 0)")
    int insert(StockSnapshot snapshot);

    /** 乐观锁更新：WHERE version = #{version}，防止并发重复更新 */
    @Update("UPDATE stock_snapshot SET current_quantity = #{currentQuantity}, total_cost = #{totalCost}, " +
            "weighted_avg_cost = #{weightedAvgCost}, costing_method = #{costingMethod}, item_name = #{itemName}, unit = #{unit}, version = version + 1 " +
            "WHERE item_code = #{itemCode} AND version = #{version}")
    int updateQuantity(StockSnapshot snapshot);
}
