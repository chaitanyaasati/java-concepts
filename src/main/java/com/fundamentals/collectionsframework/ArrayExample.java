package com.fundamentals.collectionsframework;

import java.util.Arrays;

public class ArrayExample {
    public static void main(String[] args) {
        int[] arr = new int[10];
        arr[0] = 100;
        System.out.println("Length of array: " + arr.length);
        System.out.println("Element at 0th index: " + arr[0]);
        System.out.println("Element at 9th index: " + arr[9]);

        // Ways to initialize
        int[] arr1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] arr2 = new int[]{1, 2, 3, 4, 5};
        int[] arr3 = new int[10];

        // Ways to populate data
        Arrays.fill(arr1, 8);
        Arrays.fill(arr1, 2, 6, 8); // fills index 2 to 5 with 8
    }
}
