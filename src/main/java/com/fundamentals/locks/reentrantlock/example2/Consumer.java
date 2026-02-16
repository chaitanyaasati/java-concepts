package com.fundamentals.locks.reentrantlock.example2;

import com.fundamentals.locks.reentrantlock.example1.SharedResource;

public class Consumer {

    private SharedResource sharedResource;

    public Consumer(SharedResource resource){
        this.sharedResource = resource;
    }

}
