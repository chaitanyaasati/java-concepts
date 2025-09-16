package com.fundamentals.executorframework;

public class Sequential {

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        for (int i = 1; i < 10; i++) {
            FactorialCalc.factorial(i);
        }
        long endTime = System.currentTimeMillis();
        long ms = (endTime - startTime) / 1000;
        System.out.printf("Total time is %d seconds", ms);
    }
}

// what is difference between runnable and callable?
// Runnable does not return response
// Callable does return
