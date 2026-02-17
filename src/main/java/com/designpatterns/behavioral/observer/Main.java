package com.designpatterns.behavioral.observer;

public class Main {
    public static void main(String[] args) {
        Listener<String> soundListener = new SoundListener();
        Listener<String> lightListener = new LightListener();
        HindustanTimes hindustanTimes = new HindustanTimes();
        hindustanTimes.addListener(soundListener);
        hindustanTimes.publishNewspaper("Happy republic Day");
        hindustanTimes.addListener(lightListener);
        hindustanTimes.removeListener(soundListener);
        hindustanTimes.publishNewspaper("Happy Independence Day");
        System.out.println("End of world");
    }
}
