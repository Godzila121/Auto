package com.example.myapp;

import java.io.Serializable;

public class Car implements Serializable {
    private String name;
    private String type;
    private int year;
    private String country;
    private double price;

    public Car(String name, String type, int year, String country, double price) {
        this.name = name;
        this.type = type;
        this.year = year;
        this.country = country;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getYear() {
        return year;
    }

    public String getCountry() {
        return country;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }
}
