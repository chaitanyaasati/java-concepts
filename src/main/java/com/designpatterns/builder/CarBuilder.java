package com.designpatterns.builder;

public class CarBuilder {

    private String brand;
    private String tyres;
    private int price;

    public CarBuilder brand(String brand){
        this.brand = brand;
        return this;
    }

    public CarBuilder tyres(String tyres){
        this.tyres = tyres;
        return this;
    }

    public CarBuilder price(int price){
        this.price = price;
        return this;
    }

    public Car build(){
        return new Car(brand, tyres, price);
    }
}
