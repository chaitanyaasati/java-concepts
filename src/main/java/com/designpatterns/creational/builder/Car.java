package com.designpatterns.creational.builder;

public class Car {

    private final String brand;
    private final String tyres;
    private final int price;

    // Private constructor: Car can only be created via the Builder
    private Car(Builder builder) {
        this.brand = builder.brand;
        this.tyres = builder.tyres;
        this.price = builder.price;
    }

    @Override
    public String toString() {
        return "Car{brand='" + brand + "', tyres='" + tyres + "', price=" + price + "}";
    }

    // Static Inner Builder Class
    public static class Builder {
        private String brand;
        private String tyres;
        private int price;

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder tyres(String tyres) {
            this.tyres = tyres;
            return this;
        }

        public Builder price(int price) {
            this.price = price;
            return this;
        }

        public Car build() {
            // Validation logic
            if (brand == null || brand.isEmpty()) {
                throw new IllegalStateException("Brand is mandatory");
            }
            if (price < 0) {
                throw new IllegalStateException("Price cannot be negative");
            }
            return new Car(this);
        }
    }
}
