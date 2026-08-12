package com.hakira.common.pojo.dto.person;

import com.hakira.common.pojo.dto.goods.FruitsSupMktGoods;

import java.util.List;

public class FruitsSupMktCustomer extends People {
    private static final long serialVersionUID = 1L;

    private List<FruitsSupMktGoods> goods;

    private Integer discountLevel;

    public List<FruitsSupMktGoods> getGoods() {
        return goods;
    }

    public void setGoods(List<FruitsSupMktGoods> goods) {
        this.goods = goods;
    }

    public Integer getDiscountLevel() {
        return discountLevel;
    }

    public void setDiscountLevel(Integer discountLevel) {
        this.discountLevel = discountLevel;
    }
}
