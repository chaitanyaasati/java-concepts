package com.fundamentals.multithreading_basics.projectone;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting the application");
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        Processor processor = new Processor(queue);
        Thread processorThread = new Thread(processor);
        processorThread.start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a number to add to the queue or 'exit' to quit:");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            try {
                queue.put(Integer.parseInt(input));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        scanner.close();
        processorThread.interrupt();
    }
}
