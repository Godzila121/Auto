package com.example.myapp;

import android.content.Context;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_REGISTERED = "isRegistered";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_PASSWORD = "userPassword";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; // Ключ для стану входу

    private static final String CAR_LIST_COLLECTION = "car_list";
    private static final String FAVORITES_COLLECTION = "favorite_cars";

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Збереження акаунту користувача
    public static void saveUserCredentials(Context context, String email, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_REGISTERED, true)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public static String getSavedEmail(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_EMAIL, "");
    }

    public static String getSavedPassword(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PASSWORD, "");
    }

    public static boolean isUserRegistered(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_REGISTERED, false);
    }

    // Збереження стану входу
    public static void saveLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply();
    }

    // Отримання стану входу
    public static boolean getLoginStatus(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void clearUserCredentials(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

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
                    listener.onCarListLoaded(carList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при завантаженні автомобілів", Toast.LENGTH_SHORT).show();
                    listener.onCarListLoaded(null);
                });
    }

    // Збереження улюблених
    public static void saveFavorites(Context context, List<Car> favoriteCars) {
        db.collection(FAVORITES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }

                    for (Car car : favoriteCars) {
                        db.collection(FAVORITES_COLLECTION)
                                .add(car)
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