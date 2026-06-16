package com.designpatterns.behavioral.observer;

public class Main {
    public static void main(String[] args) {
        Listener<String> soundListener = new SoundListener();
        Listener<String> lightListener = new LightListener();
        
        // A faulty listener that throws an exception
        Listener<String> faultyListener = (data) -> {
            throw new RuntimeException("Unexpected error in faulty listener!");
        };

        HindustanTimes hindustanTimes = new HindustanTimes();
        
        hindustanTimes.addListener(soundListener);
        hindustanTimes.addListener(faultyListener);
        hindustanTimes.addListener(lightListener);

        System.out.println("--- First Publication (Should see error from faulty but others succeed) ---");
        hindustanTimes.publishNewspaper("Happy Republic Day");
        
        hindustanTimes.removeListener(soundListener);
        
        System.out.println("\n--- Second Publication ---");
        hindustanTimes.publishNewspaper("Happy Independence Day");
        
        System.out.println("\nEnd of world");
    }
}
