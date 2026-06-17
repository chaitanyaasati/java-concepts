package com.fundamentals.collectionsframework.queueinterface.priorityqueuedemo;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class PriorityQueueDemo {
    public static void main(String[] args) {
        Queue<String> pq = new PriorityQueue<>(Comparator.comparingInt(String::length).reversed());
//        Queue<String> pq = new PriorityQueue<>(Comparator.comparingInt(String::length));
        pq.offer("Akshit");
        pq.offer("Shubham");
        pq.offer("Neha");
        System.out.println("Priority Queue: " + pq);

        boolean result1 = pq.add("Akshar");
        boolean result2 = pq.offer("Prakhar");
        System.out.println("Priority Queue: add method response " + result1 );
        System.out.println("Priority Queue: offer method response " + result2 );

        System.out.println("Priority Queue: " + pq);

        String result3 = pq.remove();
        System.out.println("Priority Queue Removed element: " + result3);
        System.out.println("Priority Queue: " + pq);

        String result4 = pq.poll();
        System.out.println("Priority Queue Removed element: " + result4);
        System.out.println("Priority Queue: " + pq);

        boolean isEmpty = pq.isEmpty();
        System.out.println("Priority Queue is empty: " + isEmpty);

        int size = pq.size();
        System.out.println("Priority Queue Size: " + size);

        String result5 = pq.peek();
        System.out.println("Priority Queue Peeked element: " + result5);

        String result6 = pq.element();
        System.out.println("Priority Queue Element method peeked element: " + result6);

        pq.clear();
        System.out.println("Priority Queue after clear: " + pq);
    }
}
