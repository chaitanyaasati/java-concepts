package com.fundamentals.collectionsframework.queueinterface.dequedemo;

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
        Deque<Integer> deque = new ArrayDeque<>(); // faster iteration, low memory, no null allowed, circular queue
//        Deque<Integer> deque = new LinkedList<>();
        Integer ele1 = deque.pollFirst();
        Integer ele2 = deque.pollLast();
        Integer ele3 = deque.peekFirst();
        Integer ele4 = deque.peekLast();
        System.out.println("Element Polled First: " + ele1);
        System.out.println("Element Polled Last: " + ele2);
        System.out.println("Element Peeked First: " + ele3);
        System.out.println("Element Peeked Last: " + ele4);
        deque.offerFirst(5);
        deque.offerLast(30);
        deque.offerLast(25);
        System.out.println(deque);
        Integer ele5 = deque.peekFirst();
        Integer ele6 = deque.peekLast();
        System.out.println("Element Peeked First: " + ele5);
        System.out.println("Element Peeked Last: " + ele6);
        Integer ele7 = deque.pollFirst();
        Integer ele8 = deque.pollLast();
        System.out.println("Element Polled First: " + ele7);
        System.out.println("Element Polled Last: " + ele8);
        for(int x : deque){
            System.out.println(x);
        }
    }
}
