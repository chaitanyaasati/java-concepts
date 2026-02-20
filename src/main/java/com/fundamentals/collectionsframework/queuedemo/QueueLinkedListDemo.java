package com.fundamentals.collectionsframework.queuedemo;

import java.util.LinkedList;

public class QueueLinkedListDemo {
    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<>();
        list.addLast(1); // enqueue
        list.addLast(45);// enqueue
        System.out.println("Updated List: " + list);

        list.addLast(124); // enqueue
        System.out.println("Updated List: " + list);

        System.out.println(list);
        Integer removedNumber = list.removeFirst(); // dequeue
        System.out.println("Removed Number: " + removedNumber);

        System.out.println("Updated List: " + list);
        System.out.println("Peeked: " + list.getFirst()); // peek
        System.out.println("Peeked: " + list.peekFirst());
    }
}
