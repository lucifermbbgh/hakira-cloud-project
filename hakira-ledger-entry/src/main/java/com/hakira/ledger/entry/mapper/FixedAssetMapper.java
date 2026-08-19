package com.hakira.ledger.entry.mapper;

import com.hakira.ledger.entry.pojo.FixedAsset;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 固定资产卡片 Mapper
 *
 * @author hakiraKafka
 */
@Mapper
public interface FixedAssetMapper {

    @Insert("INSERT INTO fixed_asset(asset_code, asset_name, category, original_value, residual_rate, " +
            "useful_life, depreciation_method, accumulated_depreciation, net_value, status, purchase_date, version) " +
            "VALUES(#{assetCode}, #{assetName}, #{category}, #{originalValue}, #{residualRate}, " +
            "#{usefulLife}, #{depreciationMethod}, 0, #{originalValue}, 'IN_USE', #{purchaseDate}, 0)")
    int insert(FixedAsset asset);

    @Select("SELECT * FROM fixed_asset WHERE asset_code = #{assetCode}")
    FixedAsset selectByCode(@Param("assetCode") String assetCode);

    @Select("SELECT * FROM fixed_asset ORDER BY asset_code")
    List<FixedAsset> selectAll();

    @Select("SELECT * FROM fixed_asset WHERE status = 'IN_USE' ORDER BY asset_code")
    List<FixedAsset> selectInUse();

    @Update("UPDATE fixed_asset SET accumulated_depreciation = #{accumulatedDepreciation}, " +
            "net_value = #{netValue}, version = version + 1 " +
            "WHERE asset_code = #{assetCode} AND version = #{version}")
    int updateDepreciation(@Param("assetCode") String assetCode,
                           @Param("accumulatedDepreciation") BigDecimal accumulatedDepreciation,
                           @Param("netValue") BigDecimal netValue,
                           @Param("version") int version);

    @Update("UPDATE fixed_asset SET status = 'DISPOSED', version = version + 1 " +
            "WHERE asset_code = #{assetCode} AND version = #{version}")
    int updateStatusDisposed(@Param("assetCode") String assetCode, @Param("version") int version);
}
