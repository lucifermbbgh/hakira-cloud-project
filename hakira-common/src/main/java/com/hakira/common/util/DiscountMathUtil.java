package com.hakira.common.util;

public class DiscountMathUtil {

    /**
     * 参与商品限时打折
     */
    public static final Integer DISCOUNT_LEVEL_ONE = 1;
    /**
     * 参与商品总价满减优惠
     */
    public static final Integer DISCOUNT_LEVEL_TWO = 2;

    public static final double DISCOUNT_LEVEL_TWO_PRICE = 10d;

    public static double getDiscountPrice(String goodsName, double price, Integer weight, Integer discountLevel, double discount) {
        int goodsWeight = weight.intValue();
        double disAmt = price * goodsWeight;
        if (discount < 1){
            if (discountLevel == DISCOUNT_LEVEL_ONE || discountLevel == DISCOUNT_LEVEL_TWO) {
                System.out.println("商品[" + goodsName + "]参与限时打折");
                disAmt = disAmt * discount;
                return disAmt;
            }
        }
        return disAmt;
    }

    public static double getDiscountLevelPrice(double countPrice, Integer discountLevel) {
        double discountPrice = countPrice;
        if (discountLevel == DISCOUNT_LEVEL_TWO && countPrice >= 100d) {
            System.out.println("顾客参与满减优惠");
            discountPrice = countPrice - DISCOUNT_LEVEL_TWO_PRICE;
            return discountPrice;
        }
        return discountPrice;
    }
}
