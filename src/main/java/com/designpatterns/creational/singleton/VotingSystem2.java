package com.designpatterns.creational.singleton;

public class VotingSystem2 {

    private static volatile VotingSystem2 INSTANCE;

    private VotingSystem2(){
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
}
