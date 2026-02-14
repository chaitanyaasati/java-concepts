package com.fundamentals.completablefuture;

public class Task implements Runnable{

    private String name;
    private long sleepTime;

    public Task(String name, long sleepTime){
        this.name = name;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run(){
        System.out.println("Thread " + name + " start");
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Thread " + name + " end");
    }
}
