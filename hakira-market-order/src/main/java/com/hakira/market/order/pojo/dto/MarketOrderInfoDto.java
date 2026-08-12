package com.hakira.market.order.pojo.dto;

import com.hakira.common.pojo.dto.goods.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.pojo
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  22:38:57
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketOrderInfoDto {
    private String id;// 订单ID
    private Integer status;// 订单状态
    private List<Goods> goodsList;// 商品列表
    private String goodsTotalPrice;// 商品总价
    private String orderDesc;// 订单描述
    private String createUser;// 创建用户
    private String createDate;// 创建日期
    private String createTime;// 创建时间
    private String updateUser;// 更新用户
    private String updateDate;// 更新日期
    private String updateTime;// 更新时间
}
