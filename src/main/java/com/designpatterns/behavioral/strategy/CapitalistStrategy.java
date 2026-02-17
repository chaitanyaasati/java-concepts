package com.designpatterns.behavioral.strategy;

public class CapitalistStrategy implements ParkingChargeStrategy {
    @Override
    public int calculate(int hours) {
        return hours * 3;
    }
}
