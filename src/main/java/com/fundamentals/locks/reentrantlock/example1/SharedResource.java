package com.fundamentals.locks.reentrantlock.example1;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class SharedResource {

    final private Set<Integer> dataStore;
    final private ReentrantLock lock;

    public SharedResource(){
        this.dataStore = new HashSet<>();
        this.lock = new ReentrantLock();
    }

    public void addDataStore(Integer number){
        // good practice
        // lock outside try block
        lock.lock();
        try{
            // Avoid this
            //  lock.lock();
            // If lock.lock() fails (e.g., the thread is interrupted before acquiring the lock), the finally block still runs lock.unlock() on a lock you never
            // acquired, which throws IllegalMonitorStateException. Placing lock() before try ensures unlock() only runs when the lock was actually acquired.
            dataStore.add(number);
        }
        finally{
            lock.unlock();
        }
    }

    public void removeDataStore(Integer number){
        lock.lock();
        try{
            dataStore.remove(number);
        }
        finally{
            lock.unlock();
        }
    }

    public void fetchDataStore(){
        lock.lock();
        try{
            dataStore.forEach(System.out::println);
        }
        finally{
            lock.unlock();
        }
    }
}
