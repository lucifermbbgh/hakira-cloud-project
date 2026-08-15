package com.hakira.ledger.api.asset;

import com.hakira.ledger.api.dto.asset.DepreciationResponse;
import com.hakira.ledger.api.dto.asset.DisposeResponse;
import com.hakira.ledger.api.dto.asset.FixedAssetRequest;
import com.hakira.ledger.api.dto.asset.FixedAssetResponse;

import java.util.List;

/**
 * 固定资产服务接口 — 资产卡片 / 折旧计提 / 资产处置
 */
public interface IFixedAssetService {
    /** 登记资产卡片 */
    FixedAssetResponse create(FixedAssetRequest request);

    /** 查询资产 */
    FixedAssetResponse get(String assetCode);

    /** 资产列表 */
    List<FixedAssetResponse> list();

    /** 折旧计提（按月，生成折旧凭证） */
    DepreciationResponse depreciate(String period);

    /** 资产处置/报废 */
    DisposeResponse dispose(String assetCode);
}
