package com.designpatterns.observer;

public class HindustanTimes {

    private Publisher publisher;

    public HindustanTimes(){
        publisher = new Publisher();
    }

    public void publishNewspaper(String content){
        publisher.notifyListeners(content);
    }

    public void addListener(Listener listener){
        publisher.addListener(listener);
    }

    public void removeListener(Listener listener){
        publisher.removeListener(listener);
    }
}
