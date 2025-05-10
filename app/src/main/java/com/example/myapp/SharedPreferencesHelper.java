package com.example.myapp;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SharedPreferencesHelper {

    private static final String CAR_LIST_COLLECTION = "car_list";
    private static final String FAVORITES_COLLECTION = "favorite_cars";


    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Збереження списку автомобілів у Firestore
    public static void saveCarList(Context context, List<Car> carList) {
        for (Car car : carList) {
            db.collection(CAR_LIST_COLLECTION)
                    .add(car)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Автомобіль додано в Firestore!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Помилка при додаванні автомобіля", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Завантаження списку автомобілів із Firestore
    public static void getCarList(Context context, FirebaseHelper.OnCarListLoadedListener listener) {
        db.collection(CAR_LIST_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> carList = queryDocumentSnapshots.toObjects(Car.class);
                    listener.onCarListLoaded(carList);  // Викликаємо listener з результатами
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при завантаженні автомобілів", Toast.LENGTH_SHORT).show();
                    listener.onCarListLoaded(null);  // Якщо помилка, передаємо null
                });
    }


    public static void saveFavorites(Context context, List<Car> favoriteCars) {
        db.collection(FAVORITES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Видаляємо старі документи
                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Додаємо нові улюблені машини
                    for (Car car : favoriteCars) {
                        db.collection(FAVORITES_COLLECTION)
                                .add(car)
                                .addOnSuccessListener(documentReference -> {
                                    // Ви можете показати повідомлення один раз, якщо потрібно
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Помилка при збереженні вподобань", Toast.LENGTH_SHORT).show();
                                });
                    }

                    Toast.makeText(context, "Улюблені збережено!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Не вдалося очистити старі вподобання", Toast.LENGTH_SHORT).show();
                });
    }

    public static void getFavorites(Context context, FirebaseHelper.OnCarListLoadedListener listener) {
        db.collection(FAVORITES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> carList = queryDocumentSnapshots.toObjects(Car.class);
                    listener.onCarListLoaded(carList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при завантаженні вподобань", Toast.LENGTH_SHORT).show();
                    listener.onCarListLoaded(null);
                });
    }



}
