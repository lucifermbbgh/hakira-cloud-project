package com.hakira.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SHIPPED(2, "已发货"),
    DELIVERED(3, "已交付"),
    CLOSED(4, "已关闭"),
    CANCELLED(5, "已取消"),
    REFUNDED_PART(6, "部分退款"),
    REFUNDED_ALL(7, "全部退款"),
    UNKNOWN(999, "未知");

    private final Integer code;
    private final String description;

//    OrderStatusEnum(Integer code, String description) {
//        this.code = code;
//        this.description = description;
//    }
//
//    public Integer getCode() {
//        return code;
//    }
//
//    public String getDescription() {
//        return description;
//    }

    public static OrderStatusEnum get(Integer code) {
        Iterator<OrderStatusEnum> iterator = Arrays.stream(OrderStatusEnum.values()).iterator();
        while (iterator.hasNext()) {
            OrderStatusEnum next = iterator.next();
            if (next.code.intValue() == code.intValue()) {
                return next;
            }
        }
        return null;
    }

    public static OrderStatusEnum getByLamda(Integer code) {
        Optional<OrderStatusEnum> first = Arrays.stream(OrderStatusEnum.values()).filter(e -> e.code.intValue() == code.intValue()).findFirst();
        return first.orElse(null);
    }

    public static Integer getCodeByDescription(String description) {
        Optional<OrderStatusEnum> first = Arrays.stream(OrderStatusEnum.values()).filter(e -> e.getDescription().equals(description)).findFirst();
        return first.get().getCode();
    }

    public static String getDescriptionByCode(Integer code) {
        Optional<OrderStatusEnum> first = Arrays.stream(OrderStatusEnum.values()).filter(e -> e.code.intValue() == code.intValue()).findFirst();
        return first.get().getDescription();
    }
}
