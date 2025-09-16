package com.fundamentals.executorframework;

public class Multithreading {
    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Thread[] ans = new Thread[9];
        for (int i = 1; i < 10; i++) {
            final int val = i;
            ans[i - 1] = new Thread(() -> {
                try {
                    FactorialCalc.factorial(val);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            ans[i - 1].start();
        }
        for (int i = 0; i < 9; i++) {
            ans[i].join();
        }
        long endTime = System.currentTimeMillis();
        long ms = (endTime - startTime) / 1000;
        System.out.printf("Total time is %d seconds", ms);
    }
}
