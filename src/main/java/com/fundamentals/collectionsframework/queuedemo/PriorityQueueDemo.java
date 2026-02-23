package com.fundamentals.collectionsframework.queuedemo;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class PriorityQueueDemo {
    public static void main(String[] args) {
        Queue<String> stringLength = new PriorityQueue<>(Comparator.comparingInt(String::length));
        stringLength.add("Shubham");
        stringLength.add("Akshit");
        stringLength.add("Neha");
        System.out.println(stringLength);
    }
}
