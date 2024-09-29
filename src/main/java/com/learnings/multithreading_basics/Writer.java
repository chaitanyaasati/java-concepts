package com.learnings.multithreading_basics;

public class Writer implements Runnable{
    String subject;

    public Writer(String subject){
        this.subject = subject;
    }

    @Override
    public void run() {
        for(int count = 0; count < 10; count++){
            System.out.println("Thread ID: " + Thread.currentThread().getId() + " I am writing " + subject);
        }
    }
}
