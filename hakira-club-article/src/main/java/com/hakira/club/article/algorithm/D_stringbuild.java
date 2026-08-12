package com.hakira.club.article.algorithm;

import java.util.Scanner;

public class D_stringbuild {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s1 = sc.nextLine();
        String s2 = sc.nextLine();
        StringBuffer stringBuffer = new StringBuffer("");
        System.out.println(stringBuffer.append(s1).append(s2).toString());
    }
}
