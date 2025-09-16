package com.fundamentals.executorframework;

public class FactorialCalc {

    public static long factorial(int num) throws InterruptedException {
        Thread.sleep(1000);
        if (num == 0) return 1;
        int ans = num;
        while (num != 1) {
            num = num - 1;
            ans = ans * num;
        }
        System.out.println(ans);
        return ans;
    }
}
