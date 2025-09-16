package com.fundamentals.executorframework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CachedThreadPool {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        long startTime = System.currentTimeMillis();
        for (int i = 1; i < 10; i++) {
            final int num = i;
            executor.submit(() -> FactorialCalc.factorial(num));
        }
        executor.shutdown();
        executor.awaitTermination(20000, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        long ms = (endTime - startTime) / 1000;
        System.out.printf("Total time is %d seconds", ms);
    }
}
