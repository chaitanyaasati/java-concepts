package com.designpatterns.creational.singleton;

public class VotingSystem1 {

    private static VotingSystem1 INSTANCE;

    public static VotingSystem1 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VotingSystem1();
        }
        return INSTANCE;
    }
}
