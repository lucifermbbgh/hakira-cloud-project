package com.hakira.common.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

@Getter
public enum ActStatusEnum {
    STATUS_NO_ENABLED(0, "未启用"),
    STATUS_NORMAL(1, "正常"),
    STATUS_FREEZE(2, "冻结"),
    STATUS_DELETE(3, "注销"),
    UNKNOWN(999, "未知");

    private Integer code;
    private String description;

    ActStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ActStatusEnum get(Integer code) {
        Iterator<ActStatusEnum> iterator = Arrays.stream(ActStatusEnum.values()).iterator();
        while (iterator.hasNext()) {
            ActStatusEnum next = iterator.next();
            if (next.code.intValue() == code.intValue()) {
                return next;
            }
        }
        return null;
    }

    public static ActStatusEnum getByLamda(Integer code) {
        Optional<ActStatusEnum> first = Arrays.stream(ActStatusEnum.values()).filter(e -> e.code.intValue() == code.intValue()).findFirst();
        ActStatusEnum actStatusEnum = first.orElse(null);
        return actStatusEnum;
    }

    public static Integer getCodeByDescription(String description) {
        Optional<ActStatusEnum> first = Arrays.stream(ActStatusEnum.values()).filter(e -> e.getDescription().equals(description)).findFirst();
        return first.get().getCode();
    }

    public static String getDescriptionByCode(Integer code) {
        Optional<ActStatusEnum> first = Arrays.stream(ActStatusEnum.values()).filter(e -> e.code.intValue() == code.intValue()).findFirst();
        return first.get().getDescription();
    }
}
