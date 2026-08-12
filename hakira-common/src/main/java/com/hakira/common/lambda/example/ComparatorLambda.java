package com.hakira.common.lambda.example;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.lambda
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-21  16:56:27
 * @Description: Comparator（比较器）函数式接口lambda表达式
 * @Version: 1.0
 */
public class ComparatorLambda {
    public static void main(String[] args) {
        String[] strings = {"asdf", "trherth", "zxcvxweerygwret", "yutyjt", "zxc", "yx"};
        String[] tempStrings1 = strings.clone();
        String[] tempStrings2 = strings.clone();
        String[] tempStrings3 = strings.clone();
        // 用匿名内部类实现Comparator接口
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() - o2.length();
            }
        };
        Arrays.sort(tempStrings1, comparator);
        System.out.println(Arrays.toString(tempStrings1));

        Arrays.sort(tempStrings2, (o1, o2) -> {
            return o1.length() - o2.length();
        });
        System.out.println(Arrays.toString(tempStrings2));

        Arrays.sort(tempStrings3, (o1, o2) -> o1.length() - o2.length());
        System.out.println(Arrays.toString(tempStrings3));
    }
}
