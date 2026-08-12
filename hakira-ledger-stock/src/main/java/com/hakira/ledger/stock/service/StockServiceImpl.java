package com.hakira.ledger.stock.service;

import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.dto.stock.StockMovementRequest;
import com.hakira.ledger.api.dto.stock.StockMovementResponse;
import com.hakira.ledger.api.dto.stock.StockSnapshotResponse;
import com.hakira.ledger.api.stock.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockServiceImpl implements IStockService {

    /** 库存快照存储: itemCode -> current quantity */
    private final ConcurrentHashMap<String, BigDecimal> stockSnapshot = new ConcurrentHashMap<>();
    /** 物资名称存储 */
    private final ConcurrentHashMap<String, String> itemNameStore = new ConcurrentHashMap<>();
    /** 单位存储 */
    private final ConcurrentHashMap<String, String> unitStore = new ConcurrentHashMap<>();
    /** 变动明细存储 */
    private final List<StockMovementResponse> movements = new ArrayList<>();

    @Override
    public StockMovementResponse recordInbound(StockMovementRequest request) {
        String movementId = IdGeneratorUtil.getId();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 更新库存
        BigDecimal current = stockSnapshot.getOrDefault(request.getItemCode(), BigDecimal.ZERO);
        BigDecimal newQty = current.add(request.getQuantity());
        stockSnapshot.put(request.getItemCode(), newQty);
        itemNameStore.put(request.getItemCode(), request.getItemName());
        unitStore.put(request.getItemCode(), request.getUnit());

        // 记录变动
        StockMovementResponse movement = new StockMovementResponse();
        movement.setMovementId(movementId);
        movement.setItemCode(request.getItemCode());
        movement.setItemName(request.getItemName());
        movement.setDirection("INBOUND");
        movement.setQuantity(request.getQuantity());
        movement.setUnit(request.getUnit());
        movement.setRelatedVoucherNo(request.getRelatedVoucherNo());
        movement.setMovementDate(now);
        movement.setRemark(request.getRemark());
        movements.add(movement);

        log.info("入库成功: movementId={}, itemCode={}, quantity={}, currentStock={}",
                movementId, request.getItemCode(), request.getQuantity(), newQty);
        return movement;
    }

    @Override
    public StockMovementResponse recordOutbound(StockMovementRequest request) {
        String movementId = IdGeneratorUtil.getId();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 检查库存是否足够
        BigDecimal current = stockSnapshot.getOrDefault(request.getItemCode(), BigDecimal.ZERO);
        if (current.compareTo(request.getQuantity()) < 0) {
            throw new IllegalArgumentException(
                    String.format("库存不足: itemCode=%s, 当前库存=%s, 申请出库=%s",
                            request.getItemCode(), current, request.getQuantity()));
        }

        // 更新库存
        BigDecimal newQty = current.subtract(request.getQuantity());
        stockSnapshot.put(request.getItemCode(), newQty);
        itemNameStore.put(request.getItemCode(), request.getItemName());
        unitStore.put(request.getItemCode(), request.getUnit());

        // 记录变动
        StockMovementResponse movement = new StockMovementResponse();
        movement.setMovementId(movementId);
        movement.setItemCode(request.getItemCode());
        movement.setItemName(request.getItemName());
        movement.setDirection("OUTBOUND");
        movement.setQuantity(request.getQuantity());
        movement.setUnit(request.getUnit());
        movement.setRelatedVoucherNo(request.getRelatedVoucherNo());
        movement.setMovementDate(now);
        movement.setRemark(request.getRemark());
        movements.add(movement);

        log.info("出库成功: movementId={}, itemCode={}, quantity={}, currentStock={}",
                movementId, request.getItemCode(), request.getQuantity(), newQty);
        return movement;
    }

    @Override
    public StockSnapshotResponse getSnapshot(String itemCode) {
        BigDecimal qty = stockSnapshot.getOrDefault(itemCode, BigDecimal.ZERO);
        String name = itemNameStore.getOrDefault(itemCode, "");
        String unit = unitStore.getOrDefault(itemCode, "");

        StockSnapshotResponse snapshot = new StockSnapshotResponse();
        snapshot.setItemCode(itemCode);
        snapshot.setItemName(name);
        snapshot.setCurrentQuantity(qty);
        snapshot.setUnit(unit);

        // 获取最后一次变动时间
        String lastUpdate = movements.stream()
                .filter(m -> itemCode.equals(m.getItemCode()))
                .map(StockMovementResponse::getMovementDate)
                .reduce((first, second) -> second) // 取最后一个
                .orElse(null);
        snapshot.setLastUpdateTime(lastUpdate);

        return snapshot;
    }

    @Override
    public List<StockMovementResponse> getMovements(String itemCode, String fromDate, String toDate) {
        return movements.stream()
                .filter(m -> {
                    if (itemCode != null && !itemCode.isEmpty()
                            && !itemCode.equals(m.getItemCode())) {
                        return false;
                    }
                    if (fromDate != null && m.getMovementDate() != null
                            && m.getMovementDate().compareTo(fromDate) < 0) {
                        return false;
                    }
                    if (toDate != null && m.getMovementDate() != null
                            && m.getMovementDate().compareTo(toDate) > 0) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
