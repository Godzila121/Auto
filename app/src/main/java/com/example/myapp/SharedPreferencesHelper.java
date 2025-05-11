package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_REGISTERED = "isRegistered";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_PASSWORD = "userPassword";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private static final String CAR_LIST_COLLECTION = "car_list";
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_SUBCOLLECTION = "favorites";

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Методи для роботи з SharedPreferences
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

    public static void saveLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply();
    }

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

    // Методи для роботи з Firestore
    public static void saveCarList(Context context, List<Car> carList) {
        for (Car car : carList) {
            db.collection(CAR_LIST_COLLECTION)
                    .add(car)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Автомобіль додано в Firestore!", Toast.LENGTH_SHORT).show();
                        Log.d("SharedPreferencesHelper", "Car added to Firestore with ID: " + documentReference.getId());
                        car.setId(documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Помилка при додаванні автомобіля", Toast.LENGTH_SHORT).show();
                        Log.e("SharedPreferencesHelper", "Error adding car to Firestore: ", e);
                    });
        }
    }

    public static void getCarList(final FirebaseHelper.OnCarListLoadedListener listener) {
        db.collection(CAR_LIST_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> carList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Car car = documentSnapshot.toObject(Car.class);
                        if (car != null) {
                            car.setId(documentSnapshot.getId());
                            carList.add(car);
                        }
                    }
                    listener.onCarListLoaded(carList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(null, "Помилка при отриманні автомобілів", Toast.LENGTH_SHORT).show();
                    listener.onCarListLoaded(new ArrayList<>());
                    Log.e("SharedPreferencesHelper", "Error getting car list: ", e);
                });
    }

    private static CollectionReference getUserFavoritesCollection() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return db.collection(USERS_COLLECTION).document(currentUser.getUid()).collection(FAVORITES_SUBCOLLECTION);
        } else {
            Log.w("SharedPreferencesHelper", "Користувач не увійшов у систему");
            return null;
        }
    }

    public static void addFavorite(Context context, String carId) {
        CollectionReference favoritesCollection = getUserFavoritesCollection();
        if (favoritesCollection != null) {
            favoritesCollection.document(carId).set(new Object())
                    .addOnSuccessListener(aVoid -> Log.d("SharedPreferencesHelper", "Added car with ID " + carId + " to favorites"))
                    .addOnFailureListener(e -> Toast.makeText(context, "Помилка при додаванні до улюблених", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Будь ласка, увійдіть, щоб додавати до улюбленого", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, AccountActivity.class));
        }
    }

    public static void removeFavorite(Context context, String carId) {
        CollectionReference favoritesCollection = getUserFavoritesCollection();
        if (favoritesCollection != null) {
            favoritesCollection.document(carId).delete()
                    .addOnSuccessListener(aVoid -> Log.d("SharedPreferencesHelper", "Removed car with ID " + carId + " from favorites"))
                    .addOnFailureListener(e -> Toast.makeText(context, "Помилка при видаленні з улюблених", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Будь ласка, увійдіть, щоб видаляти з улюбленого", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, AccountActivity.class));
        }
    }

    public static void getFavoriteCarIds(Context context, final FirebaseHelper.OnFavoriteCarIdsLoadedListener listener) {
        CollectionReference favoritesCollection = getUserFavoritesCollection();
        if (favoritesCollection != null) {
            favoritesCollection.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> favoriteCarIds = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            favoriteCarIds.add(documentSnapshot.getId());
                            Log.d("SharedPreferencesHelper", "Loaded favorite car ID: " + documentSnapshot.getId());
                        }
                        listener.onFavoriteCarIdsLoaded(favoriteCarIds);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Помилка завантаження улюблених", Toast.LENGTH_SHORT).show();
                        Log.e("SharedPreferencesHelper", "Error getting favorite car IDs: ", e);
                        listener.onFavoriteCarIdsLoaded(new ArrayList<>());
                    });
        } else {
            listener.onFavoriteCarIdsLoaded(new ArrayList<>());
        }
    }
    public static void getCarsByIds(Context context, List<String> carIds, final FirebaseHelper.OnCarListLoadedListener listener) {
        if (carIds == null || carIds.isEmpty()) {
            listener.onCarListLoaded(new ArrayList<>());
            return;
        }

        db.collection(CAR_LIST_COLLECTION)
                .whereIn("id", carIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> carList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Car car = documentSnapshot.toObject(Car.class);
                        if (car != null) {
                            car.setId(documentSnapshot.getId());
                            carList.add(car);
                        }
                    }
                    listener.onCarListLoaded(carList);
                });
    }
}