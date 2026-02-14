package com.designpatterns.strategy;

public class ParkingSystem {
    private ParkingCharges parkingCharges;

    public ParkingSystem(ParkingCharges parkingCharges) {
        this.parkingCharges = parkingCharges;
    }

    public void setParkingCharges(ParkingCharges parkingCharges) {
        this.parkingCharges = parkingCharges;
    }

    public int processParking(int hours) {
        return parkingCharges.calculate(hours);
    }

}
