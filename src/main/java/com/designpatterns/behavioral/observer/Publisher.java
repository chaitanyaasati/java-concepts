package com.designpatterns.behavioral.observer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Publisher<T> {

    private final Set<Listener<T>> listeners;

    public Publisher(){
        listeners = new CopyOnWriteArraySet<>();
    }

    public void addListener(Listener<T> listener){
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        listeners.add(listener);
    }

    public void removeListener(Listener<T> listener){
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        listeners.remove(listener);
    }

    public void notifyListeners(T data){
        for(Listener<T> listener : listeners){
            try {
                listener.update(data);
            } catch (Exception e) {
                System.err.println("Failed to notify listener: " + e.getMessage());
            }
        }
    }
}
