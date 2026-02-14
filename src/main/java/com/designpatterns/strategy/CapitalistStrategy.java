package com.designpatterns.strategy;

public class CapitalistStrategy implements ParkingCharges{
    @Override
    public int calculate(int hours) {
        return hours * 3;
    }
}
