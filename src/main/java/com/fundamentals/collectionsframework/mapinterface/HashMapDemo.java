package com.fundamentals.collectionsframework.mapinterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapDemo {
    public static void main(String[] args) {
        Map<Integer, String> map = new HashMap<>();
        map.put(31, "Shubham");
        map.put(11, "Akshit");
        map.put(2, "Neha");
        map.put(null, "Ram");
        // overrides previous value
        map.put(null, "Vipul");
        System.out.println(map);

        String student = map.get(31);
        System.out.println(student);

        String student2 = map.get(69);
        System.out.println(student2);

        System.out.println(map.containsKey(31));
        System.out.println(map.containsValue( "Shubham"));

        Set<Integer> keys = map.keySet();
        System.out.println(keys);

        for(Integer key : keys){
            System.out.println(map.get(key));
        }

        Set<Map.Entry<Integer, String>> entries = map.entrySet();
        System.out.println(entries);

        for(Map.Entry<Integer, String> entry : entries){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        String result1 = map.remove(2);
        System.out.println(result1);
        boolean result2 = map.remove(31, "Akshit");
        System.out.println(result2);

    }
}
