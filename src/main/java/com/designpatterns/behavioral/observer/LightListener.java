package com.designpatterns.behavioral.observer;

public class LightListener implements Listener<String>{

    @Override
    public void update(String message) {
        System.out.println("I am Light Listener with message" + message);
    }
}
