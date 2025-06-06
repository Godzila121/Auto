package com.example.myapp;

import android.util.Log;
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
    private String userId;

    public Car() {
        Log.d("Car", "Car() constructor called");
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
        Log.d("Car", "Car(...) constructor called with name: " + name);
    }

    public String getId() {
        Log.d("Car", "getId() called. Returning: " + id);
        return id;
    }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getYear() { return year; }
    public String getCountry() { return country; }
    public double getPrice() { return price; }
    public double getEngineCapacity() { return engineCapacity; }
    public int getAge() { return age; }
    public double getCustomsDuty() { return customsDuty; }
    public double getTotalPrice() { return totalPrice; }
    public String getUserId() { return userId; }

    public void setId(String id) {
        Log.d("Car", "setId() called with value: " + id);
        this.id = id;
    }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setYear(int year) { this.year = year; }
    public void setCountry(String country) { this.country = country; }
    public void setUserId(String userId) { this.userId = userId; }

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

    private void recalculate() {
        this.customsDuty = calculateCustomsDuty();
        this.totalPrice = this.price + this.customsDuty;
    }

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
                Double.compare(car.price, price) == 0 &&
                Double.compare(car.engineCapacity, engineCapacity) == 0 &&
                age == car.age &&
                Objects.equals(id, car.id) &&
                Objects.equals(name, car.name) &&
                Objects.equals(type, car.type) &&
                Objects.equals(country, car.country) &&
                Objects.equals(userId, car.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, year, country, price, engineCapacity, age, userId);
    }
}