package com.designpatterns.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(() -> {
            VotingSystem votingSystem = VotingSystem.getVotingSystem();
            System.out.println(votingSystem + " " + Thread.currentThread().getName());
        });

        executorService.submit(() -> {
            VotingSystem votingSystem = VotingSystem.getVotingSystem();
            System.out.println(votingSystem + " " + Thread.currentThread().getName());
        });

        executorService.shutdown();
    }
}
