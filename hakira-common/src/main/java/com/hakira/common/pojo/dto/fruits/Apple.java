package com.hakira.common.pojo.dto.fruits;

public class Apple extends Fruits {
    private static final long serialVersionUID = 1L;

    private static final String NAME = "苹果";

    public Apple() {
        this.setName(NAME);
        this.setPrice(8d);
    }
}
