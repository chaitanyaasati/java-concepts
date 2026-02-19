package com.fundamentals.collectionsframework.ArrayListInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayListExample {
    public static void main(String[] args) {

        // Methods to Initiate
        // Default Constructor, creates an empty ArrayList with a default capacity of 10
        ArrayList<Integer> list1 = new ArrayList<>();

        // Creates list from another list
        List<Integer> list2 = new ArrayList<>();
        list2.add(123);
        list2.add(456);
        list2.add(789);
        System.out.println("List2: " + list2);
        List<Integer> list3 = new ArrayList<>(list2);
        System.out.println("List3: " + list3);

        // or
        Integer[] list4 = new Integer[]{1,2,3};
        List<Integer> list5 = Arrays.asList(list4);
        System.out.println(list5.getClass().getName());
        // notice here - list5 class type is not ArrayList

        // or
        List<Integer> list6 = new ArrayList<>(Arrays.asList(12,45,67));

        // Creates list - fixed size, can replace element but no add and remove operation
        List<Integer> list7 = Arrays.asList(433, 436, 123);

        // Create unmodifiable list
        List<Integer> list8 = List.of(123,456,789);

        // ArrayList Operations
        List<Integer> list9 = new ArrayList<>();

        // Add Element
        list9.add(123);
        list9.add(456);
        list9.add(789);
        System.out.println("List9: " + list9);

        // Add element at particular index
        // TC = O(N)
        list9.add(1, 900);
        System.out.println("List9: " + list9);

        // Replace Element
        list9.set(1, 9000);
        System.out.println("List9: " + list9);

        // Remove Element at index
        // TC: O(N)
        list9.remove(1);
        System.out.println("List9: " + list9);

        //Remove Element
        list9.remove(Integer.valueOf(123));

        // Size of Element
        System.out.println("List9 Size: " + list9.size());

        // Trim capacity - it is ArrayList operation
        // set size of internal array to match no of elements in list
        ArrayList<Integer> list10 = new ArrayList<>(1000);
        list10.add(34);
        list10.add(13);
        list10.trimToSize();


        List<Integer> list11 = new ArrayList<>(Arrays.asList(1,2,3,4,5,6));
        // Iterating ArrayList
        for(int index = 0; index < list11.size(); index++){
            System.out.print(list11.get(index));
        }

        for(int ele : list11){
            System.out.println(ele);
        }

        // To add one list to end of another list
        List<Integer> list12 = new ArrayList<>(Arrays.asList(123,456,789));
        List<Integer> list13 = new ArrayList<>(Arrays.asList(123,567,890));
        list12.addAll(list13);
        System.out.println("List13: " + list13);

        // To check if element exists in array
        List<Integer> list14 = new ArrayList<>(Arrays.asList(190,145,125));
        System.out.println(list14.contains(45));
        System.out.println(list14.contains(125));

        // Java Lists are dynamic, but sometimes APIs need arrays. This method gives you an array containing the same elements in the same order.
        List<Integer> list15 = Arrays.asList(1, 2, 3, 4);
        Integer[] arr1 = list15.toArray(new Integer[0]);
        System.out.println(Arrays.toString(arr1));

        List<Integer> list16 = Arrays.asList(1, 2, 3, 4);
        Object[] arr2 = list16.toArray();
        System.out.println(Arrays.toString(arr2));
    }
}
