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

    // Конструктор з параметрами
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

    // Порожній конструктор для Firebase
    public Car() {
        // Порожній конструктор
    }

    // Геттери та сеттери
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return name.equals(car.name); // або інше унікальне поле
    }

    @Override
    public int hashCode() {
        return name.hashCode(); // або те саме унікальне поле
    }

    public void setPrice(double price) {
        this.price = price;
        this.customsDuty = calculateCustomsDuty(); // Перерахувати митний збір після зміни ціни
    }

    public void setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
        this.customsDuty = calculateCustomsDuty(); // Перерахувати митний збір після зміни об'єму двигуна
    }

    public void setAge(int age) {
        this.age = age;
        this.customsDuty = calculateCustomsDuty(); // Перерахувати митний збір після зміни віку
    }

    private double calculateCustomsDuty() {
        double dutyRate = 0.0;

        if (age <= 3) {
            if (engineCapacity <= 1500) {
                dutyRate = 0.10;
            } else {
                dutyRate = 0.12;
            }
        } else if (age > 3 && age <= 7) {
            if (engineCapacity <= 1500) {
                dutyRate = 0.15;
            } else {
                dutyRate = 0.18;
            }
        } else {
            if (engineCapacity <= 1500) {
                dutyRate = 0.20;
            } else {
                dutyRate = 0.25;
            }
        }

        return price * dutyRate;
    }

    public void setCustomsDuty(double customsDuty) {
        this.customsDuty = customsDuty;
    }
}
