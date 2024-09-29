package com.learnings.multithreading_basics;

import java.io.Writer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting the application");
        Writer writer1 = new Writer("History");
        Writer writer2 = new Writer("Future");
        Thread writer1Thread = new Thread(writer1);
        Thread writer2Thread = new Thread(writer2);
        System.out.println("Started both threads");
        writer1Thread.start();
        writer2Thread.start();
        writer1Thread.join();
        writer2Thread.join();
        System.out.println("Ending the Application");
    }
}