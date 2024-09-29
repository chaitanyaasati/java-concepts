package com.learnings.producer_consumer;

import java.util.Random;

public class Resource {

    boolean isAvailable;
    int ele;

    public Resource(){
        this.isAvailable = false;
    }
    public synchronized void produce(){
        System.out.println("Checking if element is already present");
        while(isAvailable){
            System.out.println("Waiting for Consumer to consume it");
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Started Production...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        isAvailable = true;
        ele = new Random().nextInt();
        System.out.println("Production completed successfully with ele " + ele);
        notify();
    }

    public synchronized void consume() {
        System.out.println("Checking if element is present");
        while(!isAvailable){
            System.out.println("Element is not present. Waiting for producer to produce it");
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Started Consumption....");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        isAvailable = false;
        System.out.println("Successfully consumed the ele " + ele);
        notify();
    }
}
