package com.hakira.common.lambda.example;

import java.util.function.Function;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.lambda
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-21  22:40:40
 * @Description: TODO
 * @Version: 1.0
 */
public class FunctionLambda {
    public static void main(String[] args) {
        andThen(new Function<String, Integer>() {
            @Override
            public Integer apply(String string) {
                return Integer.parseInt(string) + 10;
            }
        }, new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return integer + 10;
            }
        });

        andThen(
                s -> Integer.parseInt(s) + 10,
                i -> i + 20
        );

        String string = "XYZ,18";
        int ageNum = getAgeNum(string,
                s -> s.split(",")[1],
                Integer::parseInt,// 方法引用（Method Reference）
                j -> j -= 10
        );
        System.out.println("XYZ = " + ageNum);
    }

    public static void andThen(Function<String, Integer> fun1, Function<Integer, Integer> fun2) {
        // andThen方法：将多个函数合成一个函数，fun1的处理结果作为fun2的入参，返回值作为fun2的处理结果
        int applyNum = fun1.andThen(fun2).apply("10");
        System.out.println(applyNum + 20);
    }

    public static int getAgeNum(String string,
                                Function<String, String> fun1,
                                Function<String, Integer> fun2,
                                Function<Integer, Integer> fun3) {
        return fun1.andThen(fun2).andThen(fun3).apply(string);
    }
}
