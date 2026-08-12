package com.hakira.club.article.algorithm;

public class B_matrix {
    // N为奇数
    // 1≤s_{i,j}≤100(1≤i,j≤N)
    // 第 i 行第 j 列的棋盘上堆叠的棋子数量（1 ≤ i，j ≤ N）大于该棋盘上应放置的棋子数量。
    // 在第一行，给出一个整数 N，表示房间一侧的木板数量，并用半角空格分隔。
    // 给出整数 s_{i,j}，表示从第 2 行到第 N+1 行第 i 行第 j 列（1 ≤ i，j ≤ N）棋盘上堆叠的棋子数量，以半角空格分隔。
    // 输入总行数为N+1，最后换行一次。
    /*
     * 3 3 3 2 6
     * 1 8 4 6 5
     * 4 9 2 6 8
     * 7 5 8 1 6
     * */
    // N = 5 && N是奇数
    // 5行5列
    /*
     * 1 1 1 1 1
     * 1 2 2 2 1
     * 1 2 3 2 1
     * 1 2 2 2 1
     * 1 1 1 1 1
     * */
    public void cal(int n) {
        int round = Math.round(n / 2);
        for (int i = 0; i < n; i++) {// 算列式
            for (int j = 0; j < n; j++) {// 算行式

            }
        }
    }
}
