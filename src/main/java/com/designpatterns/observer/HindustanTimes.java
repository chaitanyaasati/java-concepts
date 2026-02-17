package com.designpatterns.observer;

public class HindustanTimes {

    private final Publisher publisher;

    public HindustanTimes(){
        publisher = new Publisher();
    }

    public void publishNewspaper(String content){
        publisher.notifyListeners(content);
    }

    public void addListener(Listener<String> listener){
        publisher.addListener(listener);
    }

    public void removeListener(Listener<String> listener){
        publisher.removeListener(listener);
    }
}
