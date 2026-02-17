package com.designpatterns.observer;

import java.util.HashSet;
import java.util.Set;

public class Publisher {

    private final Set<Listener<String>> listeners;

    public Publisher(){
        listeners = new HashSet<>();
    }

    public void addListener(Listener<String> listener){
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        listeners.add(listener);
    }

    public void removeListener(Listener<String> listener){
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        listeners.remove(listener);
    }

    public void notifyListeners(String data){
        for(Listener<String> listener : listeners){
            listener.update(data);
        }
    }
}
