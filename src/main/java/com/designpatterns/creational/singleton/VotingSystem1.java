package com.designpatterns.creational.singleton;

public class VotingSystem1 {

    private static VotingSystem1 INSTANCE;
    private final String greeting;

    private VotingSystem1(){
        this.greeting = "Hello Singleton";
    }

    public static VotingSystem1 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VotingSystem1();
        }
        return INSTANCE;
    }

    public String getGreeting() {
        return greeting;
    }
}
