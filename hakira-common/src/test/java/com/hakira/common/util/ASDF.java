package com.hakira.common.util;

import java.util.Scanner;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.common.util
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-23  22:38:39
 * @Description: TODO
 * @Version: 1.0
 */
public class ASDF {
    public static int climbStairs(int n) {
        if (n == 1) {
            return 1;
        } else if (n == 2) {
            return 2;
        }

        int prev1 = 1;
        int prev2 = 2;

        for (int i = 3; i <= n; i++) {
            int current = prev1 + prev2;
            prev1 = prev2;
            prev2 = current;
        }

        return prev2;
    }

    public void cal(int n) {
        int x = 1;
        int y = 2;

        for (int i = 0; i < n; i++) {

        }
    }

    public static void main(String[] args) {
        int a = 0;
        do {
            Scanner scanner = new Scanner(System.in);
            int n = scanner.nextInt(); // 可根据需要更改台阶数
            a = n;
            int result = climbStairs(n);
            System.out.println("爬到第 " + n + " 阶楼梯的方法数为: " + result);
        } while (a != 999);
    }
}
