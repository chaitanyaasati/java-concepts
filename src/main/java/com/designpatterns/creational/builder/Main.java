package com.designpatterns.creational.builder;

public class Main {
    public static void main(String[] args) {
        CarBuilder builder = new CarBuilder();
        Car car = builder
                .brand("Maruti")
                .price(34)
                .tyres("Atul")
                .build();
        System.out.println("Car " + car);
    }
}
