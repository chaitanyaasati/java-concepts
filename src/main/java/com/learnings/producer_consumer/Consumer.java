package com.learnings.producer_consumer;

public class Consumer implements Runnable{

    Resource resource;

    public Consumer(Resource resource){
        this.resource = resource;
    }

    @Override
    public void run() {
        while(true){
            resource.consume();
        }
    }
}