package com.example.myapp;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private static final String CAR_LIST_COLLECTION = "car_list"; // Колекція для списку автомобілів
    private static final String FAVORITES_COLLECTION = "favorite_cars"; // Колекція для обраних автомобілів

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Збереження списку автомобілів у Firestore
    public static void saveCarList(Context context, List<Car> carList) {
        for (Car car : carList) {
            db.collection(CAR_LIST_COLLECTION)
                    .add(car)
                    .addOnSuccessListener(documentReference -> {
                        // Якщо автомобіль додано успішно
                        Toast.makeText(context, "Автомобіль додано в Firestore!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Якщо сталася помилка
                        Toast.makeText(context, "Помилка при додаванні автомобіля в Firestore", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Отримання списку всіх автомобілів із Firestore
    public static void getCarList(Context context, OnCarListLoadedListener listener) {
        db.collection(CAR_LIST_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> carList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Car car = documentSnapshot.toObject(Car.class);
                        carList.add(car);
                    }
                    listener.onCarListLoaded(carList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при отриманні автомобілів", Toast.LENGTH_SHORT).show();
                    listener.onCarListLoaded(new ArrayList<>());
                });
    }

    // Збереження списку обраних автомобілів у Firestore
    public static void saveFavorites(Context context, List<Car> favoriteCars) {
        for (Car car : favoriteCars) {
            db.collection(FAVORITES_COLLECTION)
                    .add(car)
                    .addOnSuccessListener(documentReference -> {
                        // Якщо автомобіль додано в список обраних
                        Toast.makeText(context, "Обраний автомобіль додано в Firestore!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Якщо сталася помилка
                        Toast.makeText(context, "Помилка при додаванні обраного автомобіля в Firestore", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Отримання списку обраних автомобілів із Firestore
    public static void getFavorites(Context context, OnCarListLoadedListener listener) {
        db.collection(FAVORITES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> favoriteCars = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Car car = documentSnapshot.toObject(Car.class);
                        favoriteCars.add(car);
                    }
                    listener.onCarListLoaded(favoriteCars);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при отриманні обраних автомобілів", Toast.LENGTH_SHORT).show();
                    listener.onCarListLoaded(new ArrayList<>());
                });
    }

    // Інтерфейс для обробки отриманого списку автомобілів
    public interface OnCarListLoadedListener {
        void onCarListLoaded(List<Car> carList);
    }
}
