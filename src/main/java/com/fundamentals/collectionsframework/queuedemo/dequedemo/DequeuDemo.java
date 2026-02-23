package com.fundamentals.collectionsframework.queuedemo.dequedemo;

import java.util.ArrayDeque;
import java.util.Deque;

public class DequeuDemo {
    // double-ended queue
    // allows insertion and removal of elements from both ends

    /*
      INSERTION METHODS

      addFirst(E e): Inserts the element at the front
      addLast(E e): Inserts the specified element at the end
      offerFirst(E e): Inserts the specified element at the front if possible
      offerLast(E e): Inserts the specified element at the end if possible
     */

    /*
      REMOVAL METHODS

      removeFirst(): Retrieves and removes the first element
      removeLast(): Retrieves and removes the last element
      pollFirst(): Retrieves and removes the first element, or returns null if empty
      pollLast(): Retrieves and removes the last element, or returns null if empty
     */

    /*
      STACK METHODS
       push(E e): Adds an element at the front(equivalent to addFirst(E e))
       pop(): Removes and returns the first element(equivalent to removeFirst())
     */

    public static void main(String[] args) {
        Deque<Integer> deque1 = new ArrayDeque<>(); // faster iteration, low memory, no null allowed, circular queue
//        Deque<Integer> deque1 = new LinkedList<>();
        deque1.addFirst(10);
        deque1.addLast(20);
        deque1.offerFirst(5);
        deque1.offerLast(25);
        System.out.println(deque1);
        System.out.println("First Element: " + deque1.getFirst()); // Outputs 5
        System.out.println("Last Element: " + deque1.getLast()); // Outputs 25
        deque1.removeFirst(); // Removes 5
        deque1.pollLast(); // Removes 25

        for(int x : deque1){
            System.out.println(x);
        }
    }
}
