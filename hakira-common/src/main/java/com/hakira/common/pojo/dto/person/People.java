package com.hakira.common.pojo.dto.person;

import com.hakira.common.base.BaseDto;

public class People extends BaseDto {
    private static final long serialVersionUID = 1L;

    private String name;
    private String cash;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }
}
