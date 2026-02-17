package com.fundamentals.objectmapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    public static void main( String[] args ) throws JsonProcessingException {
//        System.out.println( "Hello World!" );
//        System.out.println("I am exploring stuff here");
//        Car car1 = new Car("dsc", "dscs", 12, "sdc");
        ObjectMapper objectMapper = new ObjectMapper();
//        String output = objectMapper.writeValueAsString(car1);
//        System.out.println("op1");
//        System.out.println(output);
//        System.out.println("op2");

//        ResourceBundle resourceBundle = ResourceBundle.getBundle("Messages", Locale.US);
//        ResourceBundle resourceBundle1 = ResourceBundle.getBundle("Messages");
//        String r = resourceBundle1.getString("cancelButton");
//        System.out.println(r);

        InputStream inputStream = App.class.getClassLoader().getResourceAsStream("to.txt");
        Scanner scanner = new Scanner(inputStream);
        String h = null;
        while (scanner.hasNextLine()) {
            h = scanner.nextLine();
            System.out.println(h);
        }
        scanner.close();
//
        Car t = objectMapper.readValue(h, Car.class);
        System.out.println(t);

        String hj = objectMapper.writeValueAsString(t);
        System.out.println(hj);

        Properties prop = new Properties();
        InputStream input = null;
        try {

            input = new FileInputStream("for.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            System.out.println(prop.getProperty("name"));
            System.out.println(prop.getProperty("password"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

//        System.out.println(System.getProperty("java.io.tmpdir"));

//        objectMapper.readValue("{"color":"Purple","name":"Nexon","noOFWheels":4,"brand":"Tata"}\\");
    }
}
