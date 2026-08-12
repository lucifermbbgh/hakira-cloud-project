package com.hakira.common.pojo.dto.fruits;

public class Mango extends Fruits {
    private static final long serialVersionUID = 1L;

    private static final String NAME = "芒果";

    public Mango() {
        this.setName(NAME);
        this.setPrice(20d);
    }
}
