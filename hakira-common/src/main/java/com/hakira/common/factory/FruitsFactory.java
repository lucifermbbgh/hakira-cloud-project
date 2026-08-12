package com.hakira.common.factory;

import com.hakira.common.base.IFactory;
import com.hakira.common.pojo.dto.fruits.Apple;
import com.hakira.common.pojo.dto.fruits.Fruits;
import com.hakira.common.pojo.dto.fruits.Mango;
import com.hakira.common.pojo.dto.fruits.Strawberry;

public class FruitsFactory implements IFactory {
    @Override
    public Fruits product(String beanName) {
        Fruits fruits = null;
        switch (beanName) {
            case "苹果":
                fruits = new Apple();
                break;
            case "草莓":
                fruits = new Strawberry();
                break;
            case "芒果":
                fruits = new Mango();
                break;
            default:
                break;
        }
        return fruits;
    }
}
