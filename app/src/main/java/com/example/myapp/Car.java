package com.example.myapp;

import java.io.Serializable;

public class Car implements Serializable {
    private String name;
    private String type; // тип машини (наприклад, "Electric", "Sedan")
    private int year;
    private String country; // країна
    private double price;

    // Оновлений конструктор без зображення
    public Car(String name, String type, int year, String country, double price) {
        this.name = name;
        this.type = type;
        this.year = year;
        this.country = country;
        this.price = price;
    }

    // Геттери
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
