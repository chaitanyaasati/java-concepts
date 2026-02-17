package com.fundamentals.objectmapper;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Car {
    private String color;
    private String name;
    private int noOfWheels;
    private String brand;

    public String getOpL1(){
        return "op1";
    }

    public String getOpL2(){
        return "op2";
    }

    public String getOpL3(){
        return "op3";
    }

}
