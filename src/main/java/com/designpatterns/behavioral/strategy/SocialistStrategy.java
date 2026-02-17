package com.designpatterns.behavioral.strategy;

public class SocialistStrategy implements ParkingChargeStrategy {
    @Override
    public int calculate(int hours) {
        return hours * 2;
    }
}
