package com.learnings.producer_consumer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting the Application");
        Resource resource = new Resource();
        Producer producer = new Producer(resource);
        Consumer consumer = new Consumer(resource);
        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();
        producerThread.start();
        System.out.println("Started both threads");
        consumerThread.join();
        producerThread.join();
        System.out.println("Ending the Application");
    }
}
