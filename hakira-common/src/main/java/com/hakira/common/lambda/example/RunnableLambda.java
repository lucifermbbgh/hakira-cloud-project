package com.hakira.common.lambda.example;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.lambda
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-21  12:18:58
 * @Description: Runnable函数式接口lambda表达式
 * @Version: 1.0
 */
public class RunnableLambda {
    public static void main(String[] args) throws InterruptedException {
        // Runnable接口线程表达式直接启动线程
        Runnable runnable = () -> {
            System.out.println("Runnable线程接口启动");
        };
        runnable.run();

        // Thread线程类实现Runnable接口来启动线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread线程启动");
            }
        }).start();

        // Thread线程类使用lambda表达式来启动线程
        new Thread(() -> {
            String name = Thread.currentThread().getName();
            System.out.println(name + " 线程启动");
        }).start();
    }
}
