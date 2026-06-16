package com.fundamentals.multithreading_basics.projectone;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private final BlockingQueue<Integer> queue;

    public Processor(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + " start");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int value = queue.take(); // blocks until an item is available
                System.out.println("Thread " + Thread.currentThread().getName() + " processed value " + value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " stopped");
    }
}
