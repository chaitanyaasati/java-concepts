package com.designpatterns.singleton;

public class VotingSystem {

    private static volatile VotingSystem INSTANCE;

    private VotingSystem(){

    }

    public static VotingSystem getVotingSystem(){
        VotingSystem result = INSTANCE;
        if(result != null){
            return result;
        }
        synchronized (VotingSystem.class){
            if(INSTANCE == null){
                INSTANCE = new VotingSystem();
            }
            return INSTANCE;
        }
    }
}
