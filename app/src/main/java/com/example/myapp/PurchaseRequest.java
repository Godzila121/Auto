// У файлі PurchaseRequest.java
package com.example.myapp;

import com.google.firebase.database.ServerValue; // Для мітки часу сервера

public class PurchaseRequest {
    private String requestId; // Унікальний ID самого запиту
    private String carId;
    private String carName;
    private String sellerId;
    private String sellerEmail; // Опціонально
    private String buyerId;
    private String buyerEmail;
    private String status; // "pending", "accepted", "declined"
    private Object timestamp; // Буде встановлено Firebase ServerValue.TIMESTAMP

    public PurchaseRequest() {
        // Порожній конструктор для Firebase
    }

    public PurchaseRequest(String carId, String carName, String sellerId, String sellerEmail, String buyerId, String buyerEmail) {
        this.carId = carId;
        this.carName = carName;
        this.sellerId = sellerId;
        this.sellerEmail = sellerEmail;
        this.buyerId = buyerId;
        this.buyerEmail = buyerEmail;
        this.status = "pending"; // Початковий статус
        this.timestamp = ServerValue.TIMESTAMP; // Встановлюємо серверний час при створенні
    }

    // Геттери
    public String getRequestId() { return requestId; }
    public String getCarId() { return carId; }
    public String getCarName() { return carName; }
    public String getSellerId() { return sellerId; }
    public String getSellerEmail() { return sellerEmail; }
    public String getBuyerId() { return buyerId; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getStatus() { return status; }
    public Object getTimestamp() { return timestamp; } // Firebase зберігає це як Long

    // Сеттери (потрібні для Firebase, якщо будете оновлювати окремі поля)
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setCarId(String carId) { this.carId = carId; }
    public void setCarName(String carName) { this.carName = carName; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
}