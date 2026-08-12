package com.hakira.common.lambda.example;

import java.util.function.Supplier;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.lambda
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-21  12:25:25
 * @Description: Supplier（供应商）函数式接口lambda表达式
 * @Version: 1.0
 */
public class SupplierLambda {
    public static void main(String[] args) {
        int array[] = {11, 3, 2, 1, 56, 987, 65, 38798, 24, 687, 6, 698, 4, 99};
        // 使用lambda表达式实现Supplier接口get()方法
        int max = getMax(() -> {
            int temp = array[0];
            for (int i = 1; i < array.length; i++) {
                if (array[i] > temp) {
                    temp = array[i];
                }
            }
            return temp;
        });
        System.out.println("max = " + max);
    }

    public static int getMax(Supplier<Integer> supplier) {
        return supplier.get();
    }
}
