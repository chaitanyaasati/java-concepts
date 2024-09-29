package com.learnings.producer_consumer;

public class Producer implements Runnable{

    Resource resource;

    public Producer(Resource resource){
        this.resource = resource;
    }

    @Override
    public void run() {
        while(true){
            resource.produce();
        }
    }
}
