package com.fundamentals.locks.readwritelock;

public class App {
    public static void main(String[] args) throws InterruptedException {
        SharedResource resource = new SharedResource();

        resource.updateResource(3400);

        Thread thread1 = new Thread(() -> {
            resource.updateResource(34);
        });
        Thread thread2 = new Thread(resource::readResource);
        Thread thread3 = new Thread(resource::readResource);
        Thread thread4 = new Thread(() -> {
            resource.updateResource(100);
        });
        Thread thread5 = new Thread(() -> {
            resource.updateResource(12);
        });
        Thread thread6 = new Thread(resource::readResource);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();
        thread6.join();

        System.out.println("Main Thread Exiting");
    }
}
