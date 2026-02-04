package com.designpatterns.observer;

import java.util.ArrayList;
import java.util.List;

public class Publisher {

    List<Listener> listenerList;

    public Publisher(){
        listenerList = new ArrayList<>();
    }

    void addListener(Listener listener){
        listenerList.add(listener);
    }

    void removeListener(Listener listener){
        listenerList.remove(listener);
    }

    void notifyListeners(String data){
        for(Listener listener : listenerList){
            listener.update(data);
        }
    }
}
