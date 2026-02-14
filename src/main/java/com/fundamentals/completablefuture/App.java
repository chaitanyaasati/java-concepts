package com.fundamentals.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Hello World");

        Task task1 = new Task("Chaitanya1", 2000);
        Task task2 = new Task("Chaitanya2", 3000);
        int b = 23;
        int c = 15;
        String name = "Chaitanya3";

        // runAsync fits better with thenRun
        CompletableFuture<Void> compFut1 = CompletableFuture.runAsync(task1)
                .thenRun(() -> System.out.println("Task1 completed"));
        CompletableFuture<Void> compFut2 = CompletableFuture.runAsync(task2)
                .thenRun(() -> System.out.println("Task2 completed"));

        // supplyAsync fits better with thenApply
        CompletableFuture<Void> compFut3 = CompletableFuture.supplyAsync(() -> {
            int a = b + c;
            System.out.println("Task3 completed " + name + " sum " + a);
            return a;
        }).thenApply((result) -> {
            System.out.println("Result " + result);
            return result + 34;
        }).thenAccept((result) -> {
            System.out.println("Result 2 " + result);
        });

        // waits for all completable future to complete
        CompletableFuture.allOf(compFut1, compFut2, compFut3).join();

        System.out.println("Completing the task");
    }
}
