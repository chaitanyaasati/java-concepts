package com.designpatterns.observer;

public class SoundListener implements Listener{

    public void update(String message){
        System.out.println("Hello I am Sound Listener. I got this message" + message);
    }
}
