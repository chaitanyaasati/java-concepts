package com.fundamentals.streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(5,4,1,2,3);
        System.out.println(list);

        // modify the list - a intermediate operation
        List<Integer> modifiedList = list.stream().map((x) -> x * 2).collect(Collectors.toList());
        System.out.println(modifiedList);

        // filter - a intermediate operation
        // collect - a terminal operation
        List<Integer> filteredList = list.stream().filter(x -> x % 2 == 0).collect(Collectors.toList());
        System.out.println(filteredList);

        // sorted() - an intermediate operation
        List<Integer> sortedList = list.stream().sorted().collect(Collectors.toList());
        System.out.println(sortedList);

        // reverse sorting
        List<Integer> reverseSortedList = list.stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        System.out.println(reverseSortedList);
    }
}
