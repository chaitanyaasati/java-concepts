package com.fundamentals.locks.readwritelock;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SharedResource {

    private ReadWriteLock readWriteLock;
    private int resource;

    public SharedResource(){
        this.readWriteLock = new ReentrantReadWriteLock();
        this.resource = 0;
    }

    public Integer readResource(){
        readWriteLock.readLock().lock();
        try{
            System.out.println("Thread " + Thread.currentThread().getName() + " Resource " + resource);
            return resource;
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void updateResource(int value){
        readWriteLock.writeLock().lock();
        try{
            resource = value;
            System.out.println("Thread " + Thread.currentThread().getName() + " Wrote Resource " + resource);
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }

}
