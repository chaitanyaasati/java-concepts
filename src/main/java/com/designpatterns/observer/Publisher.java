package com.designpatterns.observer;

import java.util.HashSet;
import java.util.Set;

public class Publisher<T> {

    private final Set<Listener<T>> listeners;

    public Publisher(){
        listeners = new HashSet<>();
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
            listener.update(data);
        }
    }
}
