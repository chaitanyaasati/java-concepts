package com.fundamentals.collectionsframework.queuedemo;


// Queue is based on FIFO
// Elements are added at the end and removed from the front

import java.util.LinkedList;
import java.util.Queue;

// enqueue -> add at tail
// dequeue -> remove from head
// peek -> head

public class QueueDemo {
    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(899);
        queue.remove(); // throws exception if empty

        Integer numberPolled = queue.poll(); // returns null if queue is empty
        System.out.println("Number Removed: " + numberPolled);
        System.out.println("Updated Queue: " + queue);

        queue.add(123);
        queue.add(567); // throws exception if insert fails when there is bounded queue fixed capacity
        queue.offer(789); // returns true/false upon success insertion/failure insertion
        System.out.println("Updated Queue: " + queue);

        queue.add(890);
        System.out.println("Updated Queue: " + queue);

        Integer numberA = queue.remove();
        System.out.println("Number Removed: " + numberA);
        System.out.println("Updated Queue: " + queue);

        Integer numberB = queue.poll();
        System.out.println("Number Removed: " + numberB);
        System.out.println("Updated Queue: " + queue);

        Integer number = queue.peek(); // throws null if empty
        System.out.println("Number Peeked: " + number);

        Integer number1 = queue.element(); // throws exception if empty
        System.out.println("Number Peeked: " + number1);

        queue.add(581);
        System.out.println("Updated Queue: " + queue);

        queue.add(5612);
        System.out.println("Updated Queue: " + queue);
        int size = queue.size();
        System.out.println("Queue Size: " + size);
    }
}
