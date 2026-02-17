package com.designpatterns.behavioral.strategy;

public class Main {
    public static void main(String[] args) {
        ParkingChargeStrategy charges = new CapitalistStrategy();
        ParkingSystem system = new ParkingSystem(charges);
        int a1 = system.processParking(4);
        charges = new SocialistStrategy();
        system.setParkingCharges(charges);
        int a2 = system.processParking(4);
        System.out.println("A1 Capitalist Strategy " + a1);
        System.out.println("A2 Socialist Strategy " + a2);
    }
}
