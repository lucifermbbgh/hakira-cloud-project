package com.hakira.ledger.stock.service;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.dto.stock.StockMovementRequest;
import com.hakira.ledger.api.dto.stock.StockMovementResponse;
import com.hakira.ledger.api.dto.stock.StockSnapshotResponse;
import com.hakira.ledger.api.dto.stock.StocktakeRequest;
import com.hakira.ledger.api.stock.IStockService;
import com.hakira.ledger.stock.mapper.StockMovementMapper;
import com.hakira.ledger.stock.mapper.StockSnapshotMapper;
import com.hakira.ledger.stock.pojo.StockMovement;
import com.hakira.ledger.stock.pojo.StockSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存服务实现（MySQL 持久化：流水表 append + 快照表乐观锁更新）
 * <p>
 * Phase 14 新增：移动加权平均计价 —— 入库记录单价更新加权平均，出库按加权平均计算成本，
 * 盘点盘盈盘亏调整数量与成本。
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements IStockService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StockMovementMapper stockMovementMapper;
    private final StockSnapshotMapper stockSnapshotMapper;
    private final CostTransferService costTransferService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockMovementResponse recordInbound(StockMovementRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String movementId = IdGeneratorUtil.getId();
        BigDecimal quantity = request.getQuantity();
        BigDecimal unitCost = nz(request.getUnitCost());
        BigDecimal inboundCost = quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);

        StockSnapshot snapshot = stockSnapshotMapper.selectByItemCode(request.getItemCode());
        if (snapshot == null) {
            // 首次入库：insert 快照（数量 + 成本 + 加权平均单价）
            snapshot = new StockSnapshot();
            snapshot.setItemCode(request.getItemCode());
            snapshot.setItemName(request.getItemName());
            snapshot.setCurrentQuantity(quantity);
            snapshot.setTotalCost(inboundCost);
            snapshot.setWeightedAvgCost(quantity.compareTo(BigDecimal.ZERO) > 0
                    ? inboundCost.divide(quantity, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            snapshot.setUnit(request.getUnit());
            stockSnapshotMapper.insert(snapshot);
        } else {
            // 已有快照：移动加权平均重算
            BigDecimal oldQty = nz(snapshot.getCurrentQuantity());
            BigDecimal oldCost = nz(snapshot.getTotalCost());
            BigDecimal newQty = oldQty.add(quantity);
            BigDecimal newCost = oldCost.add(inboundCost);
            snapshot.setCurrentQuantity(newQty);
            snapshot.setTotalCost(newCost);
            snapshot.setWeightedAvgCost(newQty.compareTo(BigDecimal.ZERO) > 0
                    ? newCost.divide(newQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            snapshot.setItemName(request.getItemName());
            snapshot.setUnit(request.getUnit());
            if (stockSnapshotMapper.updateQuantity(snapshot) == 0) {
                throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, request.getItemCode());
            }
        }

        StockMovement movement = buildMovement(movementId, request, "INBOUND", now.toLocalDate());
        movement.setUnitCost(unitCost);
        movement.setTotalCost(inboundCost);
        stockMovementMapper.insert(movement);

        log.info("入库成功: movementId={}, itemCode={}, quantity={}, 单价={}, 成本={}, 加权平均={}",
                movementId, request.getItemCode(), quantity, unitCost, inboundCost, snapshot.getWeightedAvgCost());
        return buildMovementResponse(movement, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockMovementResponse recordOutbound(StockMovementRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String movementId = IdGeneratorUtil.getId();
        BigDecimal quantity = request.getQuantity();

        StockSnapshot snapshot = stockSnapshotMapper.selectByItemCode(request.getItemCode());
        BigDecimal current = snapshot != null ? nz(snapshot.getCurrentQuantity()) : BigDecimal.ZERO;
        if (current.compareTo(quantity) < 0) {
            throw new BizException(BizErrorCode.STOCK_INSUFFICIENT,
                    String.format("itemCode=%s, 当前库存=%s, 申请出库=%s", request.getItemCode(), current, quantity));
        }

        // 出库成本 = 数量 × 当前加权平均单价（移动加权平均，出库不改变单价）
        BigDecimal avgCost = snapshot.getWeightedAvgCost() != null ? snapshot.getWeightedAvgCost() : BigDecimal.ZERO;
        BigDecimal outboundCost = quantity.multiply(avgCost).setScale(2, RoundingMode.HALF_UP);

        snapshot.setCurrentQuantity(current.subtract(quantity));
        snapshot.setTotalCost(nz(snapshot.getTotalCost()).subtract(outboundCost));
        if (stockSnapshotMapper.updateQuantity(snapshot) == 0) {
            throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, request.getItemCode());
        }

        StockMovement movement = buildMovement(movementId, request, "OUTBOUND", now.toLocalDate());
        movement.setUnitCost(avgCost);
        movement.setTotalCost(outboundCost);
        stockMovementMapper.insert(movement);

        // 出库成本结转（生成结转凭证：借 6401 主营业务成本 / 贷 1405 库存商品）
        costTransferService.transferOutboundCost(request.getItemCode(), request.getItemName(),
                outboundCost, now.toLocalDate());

        log.info("出库成功: movementId={}, itemCode={}, quantity={}, 出库成本={}, 当前库存={}",
                movementId, request.getItemCode(), quantity, outboundCost, snapshot.getCurrentQuantity());
        return buildMovementResponse(movement, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockMovementResponse stocktake(StocktakeRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String movementId = IdGeneratorUtil.getId();
        BigDecimal actualQty = request.getActualQuantity();

        StockSnapshot snapshot = stockSnapshotMapper.selectByItemCode(request.getItemCode());
        BigDecimal currentQty = snapshot != null ? nz(snapshot.getCurrentQuantity()) : BigDecimal.ZERO;
        BigDecimal diff = actualQty.subtract(currentQty); // >0 盘盈，<0 盘亏

        BigDecimal avgCost = snapshot != null && snapshot.getWeightedAvgCost() != null
                ? snapshot.getWeightedAvgCost() : BigDecimal.ZERO;
        BigDecimal diffCost = diff.multiply(avgCost).setScale(2, RoundingMode.HALF_UP);

        String direction = diff.compareTo(BigDecimal.ZERO) >= 0 ? "STOCKTAKE_GAIN" : "STOCKTAKE_LOSS";

        if (snapshot == null) {
            snapshot = new StockSnapshot();
            snapshot.setItemCode(request.getItemCode());
            snapshot.setItemName(request.getItemName());
            snapshot.setCurrentQuantity(actualQty);
            snapshot.setTotalCost(BigDecimal.ZERO);
            snapshot.setWeightedAvgCost(BigDecimal.ZERO);
            snapshot.setUnit(request.getUnit());
            stockSnapshotMapper.insert(snapshot);
        } else {
            snapshot.setCurrentQuantity(actualQty);
            snapshot.setTotalCost(nz(snapshot.getTotalCost()).add(diffCost));
            if (stockSnapshotMapper.updateQuantity(snapshot) == 0) {
                throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, request.getItemCode());
            }
        }

        StockMovement movement = new StockMovement();
        movement.setMovementId(movementId);
        movement.setItemCode(request.getItemCode());
        movement.setItemName(request.getItemName());
        movement.setDirection(direction);
        movement.setQuantity(diff.abs());
        movement.setUnitCost(avgCost);
        movement.setTotalCost(diffCost.abs());
        movement.setUnit(request.getUnit());
        movement.setMovementDate(now.toLocalDate());
        movement.setRemark(request.getRemark());
        stockMovementMapper.insert(movement);

        log.info("盘点: itemCode={}, 差异={}, 方向={}, 成本差异={}",
                request.getItemCode(), diff, direction, diffCost.abs());
        return buildMovementResponse(movement, now);
    }

    @Override
    public StockSnapshotResponse getSnapshot(String itemCode) {
        StockSnapshot snapshot = stockSnapshotMapper.selectByItemCode(itemCode);
        StockSnapshotResponse response = new StockSnapshotResponse();
        response.setItemCode(itemCode);
        if (snapshot != null) {
            response.setItemName(snapshot.getItemName());
            response.setCurrentQuantity(snapshot.getCurrentQuantity());
            response.setTotalCost(nz(snapshot.getTotalCost()));
            response.setWeightedAvgCost(nz(snapshot.getWeightedAvgCost()));
            response.setUnit(snapshot.getUnit());
            response.setLastUpdateTime(snapshot.getUpdateTime() != null
                    ? snapshot.getUpdateTime().format(DATETIME_FORMATTER) : null);
        } else {
            response.setItemName("");
            response.setCurrentQuantity(BigDecimal.ZERO);
            response.setTotalCost(BigDecimal.ZERO);
            response.setWeightedAvgCost(BigDecimal.ZERO);
            response.setUnit("");
        }
        return response;
    }

    @Override
    public List<StockMovementResponse> getMovements(String itemCode, String fromDate, String toDate) {
        return stockMovementMapper.search(itemCode, fromDate, toDate).stream()
                .map(m -> buildMovementResponse(m, null))
                .collect(Collectors.toList());
    }

    private StockMovement buildMovement(String movementId, StockMovementRequest request,
                                        String direction, LocalDate movementDate) {
        StockMovement movement = new StockMovement();
        movement.setMovementId(movementId);
        movement.setItemCode(request.getItemCode());
        movement.setItemName(request.getItemName());
        movement.setDirection(direction);
        movement.setQuantity(request.getQuantity());
        movement.setUnit(request.getUnit());
        movement.setRelatedVoucherNo(request.getRelatedVoucherNo());
        movement.setMovementDate(movementDate);
        movement.setRemark(request.getRemark());
        return movement;
    }

    private StockMovementResponse buildMovementResponse(StockMovement movement, LocalDateTime now) {
        StockMovementResponse response = new StockMovementResponse();
        response.setMovementId(movement.getMovementId());
        response.setItemCode(movement.getItemCode());
        response.setItemName(movement.getItemName());
        response.setDirection(movement.getDirection());
        response.setQuantity(movement.getQuantity());
        response.setUnitCost(movement.getUnitCost());
        response.setTotalCost(movement.getTotalCost());
        response.setUnit(movement.getUnit());
        response.setRelatedVoucherNo(movement.getRelatedVoucherNo());
        if (now != null) {
            response.setMovementDate(now.format(DATETIME_FORMATTER));
        } else if (movement.getCreateTime() != null) {
            response.setMovementDate(movement.getCreateTime().format(DATETIME_FORMATTER));
        } else {
            response.setMovementDate(movement.getMovementDate() != null ? movement.getMovementDate().toString() : null);
        }
        response.setRemark(movement.getRemark());
        return response;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
