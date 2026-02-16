package com.fundamentals.locks.reentrantlock.example1;

public class App {
    public static void main(String[] args) throws InterruptedException {
        SharedResource sharedResource = new SharedResource();

        Thread thread1 = new Thread(() -> {
            System.out.println("Adding 245 to dataSource");
            sharedResource.addDataStore(245);
        });

        Thread thread2 = new Thread(() -> {
            System.out.println("Adding 5671 to dataSource");
            sharedResource.addDataStore(5671);
        });

        Thread thread3 = new Thread(() -> {
            System.out.println("Removing 245 to dataSource");
            sharedResource.removeDataStore(245);
        });

        Thread thread4 = new Thread(() -> {
            System.out.println("Removing 245 to dataSource");
            sharedResource.removeDataStore(245);
        });

        Thread thread5 = new Thread(() -> {
            System.out.println("Adding 2450 to dataSource");
            sharedResource.addDataStore(2450);
        });

        Thread thread6 = new Thread(() -> {
            System.out.println("View HashSet");
            sharedResource.fetchDataStore();
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();

        System.out.println("Waiting for all threads to finish");

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();
        thread6.join();

        System.out.println("Program exiting");
    }
}
