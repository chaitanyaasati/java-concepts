package com.designpatterns.strategy;

public class SocialistStrategy implements ParkingCharges{
    @Override
    public int calculate(int hours) {
        return hours * 2;
    }
}
