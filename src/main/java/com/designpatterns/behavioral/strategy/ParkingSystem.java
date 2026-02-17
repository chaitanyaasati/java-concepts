package com.designpatterns.behavioral.strategy;

public class ParkingSystem {
    private ParkingChargeStrategy parkingChargeStrategy;

    public ParkingSystem(ParkingChargeStrategy parkingChargeStrategy) {
        if (parkingChargeStrategy == null) throw new IllegalArgumentException("Strategy cannot be null");
        this.parkingChargeStrategy = parkingChargeStrategy;
    }

    public void setParkingCharges(ParkingChargeStrategy parkingChargeStrategy) {
        if (parkingChargeStrategy == null) throw new IllegalArgumentException("Strategy cannot be null");
        this.parkingChargeStrategy = parkingChargeStrategy;
    }

    public int processParking(int hours) {
        if (hours < 0) throw new IllegalArgumentException("Hours cannot be negative");
        return parkingChargeStrategy.calculate(hours);
    }

}
