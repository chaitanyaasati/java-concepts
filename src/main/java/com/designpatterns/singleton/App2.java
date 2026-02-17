package com.designpatterns.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App2 {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(() -> {
            VotingSystem2 votingSystem2 = VotingSystem2.getVotingSystem();
            System.out.println(votingSystem2 + " " + Thread.currentThread().getName());
        });

        executorService.submit(() -> {
            VotingSystem2 votingSystem2 = VotingSystem2.getVotingSystem();
            System.out.println(votingSystem2 + " " + Thread.currentThread().getName());
        });

        executorService.shutdown();
        if(executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)){
            executorService.shutdownNow();
        };
        executorService.shutdownNow();
    }
}
