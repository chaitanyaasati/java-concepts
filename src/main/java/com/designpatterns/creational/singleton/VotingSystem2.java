package com.designpatterns.creational.singleton;

public class VotingSystem2 {

    private static volatile VotingSystem2 INSTANCE;
    private final String greeting;

    private VotingSystem2(){
        this.greeting = "Hello Singleton";
    }

    public static VotingSystem2 getVotingSystem(){
        if(INSTANCE == null){
            synchronized (VotingSystem2.class){
                if(INSTANCE == null){
                    INSTANCE = new VotingSystem2();
                }
            }
        }
        return INSTANCE;
    }

    public String getGreeting() {
        return greeting;
    }
}
