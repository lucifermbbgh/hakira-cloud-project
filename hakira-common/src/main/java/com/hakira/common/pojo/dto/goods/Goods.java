package com.hakira.common.pojo.dto.goods;

import com.hakira.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goods extends BaseDto {
    private static final long serialVersionUID = 1L;
    private BigDecimal price;// 价格
    private Integer weight;// 重量
    private Integer quantity;// 数量
    private String unit;// 计量单位
    private BigDecimal discount;// 折扣
}
