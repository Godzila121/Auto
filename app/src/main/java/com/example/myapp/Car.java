package com.example.myapp;

import java.io.Serializable;

public class Car implements Serializable {
    private String name;
    private String type;
    private int year;
    private String country;
    private double price;
    private int engineCapacity;
    private int age;
    private double customsDuty;

    public Car(String name, String type, int year, String country, double price, int engineCapacity, int age) {
        this.name = name;
        this.type = type;
        this.year = year;
        this.country = country;
        this.price = price;
        this.engineCapacity = engineCapacity;
        this.age = age;
        this.customsDuty = calculateCustomsDuty();
    }

    // Геттери та сеттери (без змін)

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

    public int getEngineCapacity() {
        return engineCapacity;
    }

    public int getAge() {
        return age;
    }

    public double getCustomsDuty() {
        return customsDuty;
    }

    public void setName(String name) {
        this.name = name;
    }

    private double calculateCustomsDuty() {
        double dutyRate;
        if (age <= 3) {
            dutyRate = 0.10; // 10% для нових авто
        } else if (age > 3 && age <= 7) {
            dutyRate = 0.15; // 15% для авто середнього віку
        } else {
            dutyRate = 0.20; // 20% для старих авто
        }
        return price * dutyRate;
    }

    public void setCustomsDuty(double customsDuty) {
        this.customsDuty = customsDuty;
    }
}