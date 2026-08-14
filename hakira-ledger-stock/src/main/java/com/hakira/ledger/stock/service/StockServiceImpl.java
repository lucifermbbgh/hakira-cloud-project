package com.hakira.ledger.stock.service;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.dto.stock.StockMovementRequest;
import com.hakira.ledger.api.dto.stock.StockMovementResponse;
import com.hakira.ledger.api.dto.stock.StockSnapshotResponse;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存服务实现（MySQL 持久化：流水表 append + 快照表乐观锁更新）
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockMovementResponse recordInbound(StockMovementRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String movementId = IdGeneratorUtil.getId();
        BigDecimal quantity = request.getQuantity();

        // 更新快照（乐观锁）
        StockSnapshot snapshot = stockSnapshotMapper.selectByItemCode(request.getItemCode());
        if (snapshot == null) {
            // 首次入库：insert 快照
            snapshot = new StockSnapshot();
            snapshot.setItemCode(request.getItemCode());
            snapshot.setItemName(request.getItemName());
            snapshot.setCurrentQuantity(quantity);
            snapshot.setUnit(request.getUnit());
            stockSnapshotMapper.insert(snapshot);
        } else {
            // 已有快照：乐观锁 update（version 校验防并发重复更新）
            snapshot.setCurrentQuantity(snapshot.getCurrentQuantity().add(quantity));
            snapshot.setItemName(request.getItemName());
            snapshot.setUnit(request.getUnit());
            if (stockSnapshotMapper.updateQuantity(snapshot) == 0) {
                throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, request.getItemCode());
            }
        }

        // 记录流水
        StockMovement movement = buildMovement(movementId, request, "INBOUND", now.toLocalDate());
        stockMovementMapper.insert(movement);

        log.info("入库成功: movementId={}, itemCode={}, quantity={}, currentStock={}",
                movementId, request.getItemCode(), quantity, snapshot.getCurrentQuantity());
        return buildMovementResponse(movement, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockMovementResponse recordOutbound(StockMovementRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String movementId = IdGeneratorUtil.getId();
        BigDecimal quantity = request.getQuantity();

        // 校验库存
        StockSnapshot snapshot = stockSnapshotMapper.selectByItemCode(request.getItemCode());
        BigDecimal current = snapshot != null ? snapshot.getCurrentQuantity() : BigDecimal.ZERO;
        if (current.compareTo(quantity) < 0) {
            throw new BizException(BizErrorCode.STOCK_INSUFFICIENT,
                    String.format("itemCode=%s, 当前库存=%s, 申请出库=%s", request.getItemCode(), current, quantity));
        }

        // 乐观锁更新快照
        snapshot.setCurrentQuantity(current.subtract(quantity));
        if (stockSnapshotMapper.updateQuantity(snapshot) == 0) {
            throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, request.getItemCode());
        }

        // 记录流水
        StockMovement movement = buildMovement(movementId, request, "OUTBOUND", now.toLocalDate());
        stockMovementMapper.insert(movement);

        log.info("出库成功: movementId={}, itemCode={}, quantity={}, currentStock={}",
                movementId, request.getItemCode(), quantity, snapshot.getCurrentQuantity());
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
            response.setUnit(snapshot.getUnit());
            response.setLastUpdateTime(snapshot.getUpdateTime() != null
                    ? snapshot.getUpdateTime().format(DATETIME_FORMATTER) : null);
        } else {
            response.setItemName("");
            response.setCurrentQuantity(BigDecimal.ZERO);
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
                                        String direction, java.time.LocalDate movementDate) {
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
}
