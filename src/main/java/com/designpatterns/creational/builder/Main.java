package com.designpatterns.creational.builder;

public class Main {
    public static void main(String[] args) {
        // Using the static inner builder
        Car car = new Car.Builder()
                .brand("Maruti")
                .price(34)
                .tyres("Atul")
                .build();
        
        System.out.println(car);
        
        // This would now fail at runtime because of validation:
        // Car invalidCar = new Car.Builder().price(-10).build();
    }
}
