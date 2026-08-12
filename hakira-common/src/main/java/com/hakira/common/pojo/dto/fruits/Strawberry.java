package com.hakira.common.pojo.dto.fruits;

public class Strawberry extends Fruits {
    private static final long serialVersionUID = 1L;

    private static final String NAME = "草莓";

    public Strawberry() {
        this.setName(NAME);
        this.setPrice(13d);
    }
}
