package com.example.myapp;

import java.io.Serializable;
import java.util.Objects;

public class Car implements Serializable {
    private String id;
    private String name;
    private String type;
    private int year;
    private String country;
    private double price;
    private double engineCapacity;
    private int age;
    private double customsDuty;
    private double totalPrice;

    public Car() {
        // Порожній конструктор для Firebase
    }

    public Car(String name, String type, int year, String country, double price, double engineCapacity, int age) {
        this.name = name;
        this.type = type;
        this.year = year;
        this.country = country;
        this.price = price;
        this.engineCapacity = engineCapacity;
        this.age = age;
        recalculate();
    }

    // Геттери
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getYear() { return year; }
    public String getCountry() { return country; }
    public double getPrice() { return price; }
    public double getEngineCapacity() { return engineCapacity; }
    public int getAge() { return age; }
    public double getCustomsDuty() { return customsDuty; }
    public double getTotalPrice() { return totalPrice; }

    // Сеттери
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setYear(int year) { this.year = year; }
    public void setCountry(String country) { this.country = country; }

    public void setPrice(double price) {
        this.price = price;
        recalculate();
    }

    public void setEngineCapacity(double engineCapacity) {
        this.engineCapacity = engineCapacity;
        recalculate();
    }

    public void setAge(int age) {
        this.age = age;
        recalculate();
    }

    // Метод для оновлення мита та загальної ціни
    private void recalculate() {
        this.customsDuty = calculateCustomsDuty();
        this.totalPrice = this.price + this.customsDuty;
    }

    // Обчислення мита
    private double calculateCustomsDuty() {
        double dutyRate;

        if (age <= 3) {
            dutyRate = (engineCapacity <= 1.5) ? 0.10 : 0.12;
        } else if (age <= 7) {
            dutyRate = (engineCapacity <= 1.5) ? 0.15 : 0.18;
        } else {
            dutyRate = (engineCapacity <= 1.5) ? 0.20 : 0.25;
        }

        return price * dutyRate;
    }

    // equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car)) return false;
        Car car = (Car) o;
        return year == car.year &&
                Objects.equals(name, car.name) &&
                Objects.equals(country, car.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, year, country);
    }
}
