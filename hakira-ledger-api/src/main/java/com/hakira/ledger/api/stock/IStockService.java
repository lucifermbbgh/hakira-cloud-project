package com.hakira.ledger.api.stock;

import com.hakira.ledger.api.dto.stock.StockMovementRequest;
import com.hakira.ledger.api.dto.stock.StockMovementResponse;
import com.hakira.ledger.api.dto.stock.StockSnapshotResponse;
import com.hakira.ledger.api.dto.stock.StocktakeRequest;

import java.util.List;

/**
 * 库存台账服务接口
 */
public interface IStockService {
    /** 物资入库 */
    StockMovementResponse recordInbound(StockMovementRequest request);

    /** 物资出库 */
    StockMovementResponse recordOutbound(StockMovementRequest request);

    /** 存货盘点（盘盈盘亏） */
    StockMovementResponse stocktake(StocktakeRequest request);

    /** 库存快照（当前库存量 + 成本） */
    StockSnapshotResponse getSnapshot(String itemCode);

    /** 查询物资变动记录 */
    List<StockMovementResponse> getMovements(String itemCode, String fromDate, String toDate);
}
