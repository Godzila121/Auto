package com.example.myapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull; // Додано для Realtime Database

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

// Імпорти для Realtime Database
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; // Додано для Realtime Database

public class FirebaseHelper {

    // Константа для колекції автомобілів у Firestore (якщо використовується для чогось іншого, наприклад, додавання нових)
    private static final String CAR_LIST_COLLECTION_FIRESTORE = "car_list_firestore"; // Змінено назву для ясності
    // Назва вузла для автомобілів у Realtime Database
    private static final String CAR_LIST_NODE_RTDB = "cars";

    private static final String USERS_COLLECTION = "users"; // Для Firestore (улюблені)
    private static final String FAVORITES_SUBCOLLECTION = "favorites"; // Для Firestore (улюблені)

    private static FirebaseFirestore dbFirestore = FirebaseFirestore.getInstance(); // Firestore instance
    // Firebase Realtime Database instance не потрібен як статичне поле, отримуватимемо за потреби

    private static CollectionReference getUserFavoritesCollection() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return dbFirestore.collection(USERS_COLLECTION).document(currentUser.getUid()).collection(FAVORITES_SUBCOLLECTION);
        } else {
            Log.w("FirebaseHelper", "Користувач не увійшов у систему (для Firestore операцій)");
            return null;
        }
    }

    // Метод для збереження нового автомобіля у Firestore (якщо ви все ще хочете цю функцію для Firestore)
    // Якщо автомобілі ТІЛЬКИ в RTDB, цей метод потрібно переписати для RTDB або видалити.
    public static void saveCarToFirestore(Context context, Car car) { // Перейменовано для ясності
        // ПОПЕРЕДЖЕННЯ: Цей метод зберігає в Firestore. Якщо основні дані в RTDB, переконайтеся, що це бажана поведінка.
        dbFirestore.collection(CAR_LIST_COLLECTION_FIRESTORE)
                .add(car)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Автомобіль додано у Firestore!", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseHelper", "Автомобіль додано у Firestore з ID: " + documentReference.getId());
                    car.setId(documentReference.getId()); // Оновлюємо ID об'єкта Car
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при додаванні автомобіля у Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseHelper", "Помилка додавання автомобіля у Firestore: ", e);
                });
    }

    // Отримання списку всіх автомобілів із Firestore (якщо використовується)
    // Якщо автомобілі ТІЛЬКИ в RTDB, цей метод потрібно переписати для RTDB або видалити.
    public static void getCarListFromFirestore(final OnCarListLoadedListener listener) { // Перейменовано для ясності
        // ПОПЕРЕДЖЕННЯ: Цей метод завантажує з Firestore.
        dbFirestore.collection(CAR_LIST_COLLECTION_FIRESTORE)
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
                    // Toast.makeText(null, "Помилка при отриманні автомобілів", Toast.LENGTH_SHORT).show(); // Видалено Toast з null context
                    Log.e("FirebaseHelper", "Помилка отримання списку автомобілів з Firestore: ", e);
                    listener.onCarListLoaded(new ArrayList<>()); // Повертаємо порожній список при помилці
                });
    }

    // Додавання ID автомобіля до улюблених поточного користувача (залишається для Firestore)
    public static void addFavorite(Context context, String carId) {
        CollectionReference favoritesCollection = getUserFavoritesCollection();
        if (favoritesCollection != null) {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("addedAt", FieldValue.serverTimestamp());
            favoritesCollection.document(carId).set(favoriteData)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Автомобіль з ID " + carId + " додано до улюблених (Firestore)"))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", "Помилка при додаванні до улюблених (Firestore): " + carId, e);
                        Toast.makeText(context, "Помилка при додаванні до улюблених", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Будь ласка, увійдіть, щоб додавати до улюбленого", Toast.LENGTH_SHORT).show();
        }
    }

    // Видалення ID автомобіля з улюблених поточного користувача (залишається для Firestore)
    public static void removeFavorite(Context context, String carId) {
        CollectionReference favoritesCollection = getUserFavoritesCollection();
        if (favoritesCollection != null) {
            favoritesCollection.document(carId).delete()
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Автомобіль з ID " + carId + " видалено з улюблених (Firestore)"))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", "Помилка видалення з улюблених (Firestore): " + carId, e);
                        Toast.makeText(context, "Помилка при видаленні з улюблених", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Будь ласка, увійдіть, щоб видаляти з улюбленого", Toast.LENGTH_SHORT).show();
        }
    }

    // Отримання списку ID улюблених автомобілів поточного користувача (залишається для Firestore)
    public static void getFavoriteCarIds(final OnFavoriteCarIdsLoadedListener listener) {
        CollectionReference favoritesCollection = getUserFavoritesCollection();
        if (favoritesCollection != null) {
            favoritesCollection.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> favoriteCarIds = new ArrayList<>();
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                favoriteCarIds.add(documentSnapshot.getId());
                            }
                        }
                        listener.onFavoriteCarIdsLoaded(favoriteCarIds);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", "Помилка при отриманні ID улюблених автомобілів (Firestore)", e);
                        listener.onFavoriteCarIdsLoaded(new ArrayList<>());
                    });
        } else {
            listener.onFavoriteCarIdsLoaded(new ArrayList<>());
        }
    }

    // === МОДИФІКОВАНИЙ МЕТОД для завантаження деталей автомобілів з REALTIME DATABASE ===
    public static void getCarsByIds(List<String> carIds, final OnCarListLoadedListener listener) {
        if (carIds == null || carIds.isEmpty()) {
            Log.d("FirebaseHelper", "getCarsByIds (RTDB): Список carIds порожній.");
            listener.onCarListLoaded(new ArrayList<>());
            return;
        }

        DatabaseReference rtdbCarsRef = FirebaseDatabase.getInstance().getReference(CAR_LIST_NODE_RTDB);
        List<Car> carList = new ArrayList<>();
        // Використовуємо AtomicInteger для безпечного підрахунку асинхронних операцій
        AtomicInteger lookupsRemaining = new AtomicInteger(carIds.size());

        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Завантаження " + carIds.size() + " автомобілів з Realtime Database.");

        for (String carId : carIds) {
            if (carId == null || carId.trim().isEmpty()) {
                Log.w("FirebaseHelper", "getCarsByIds (RTDB): Пропущено порожній або null carId.");
                if (lookupsRemaining.decrementAndGet() == 0) {
                    listener.onCarListLoaded(carList);
                }
                continue;
            }

            Log.d("FirebaseHelper", "getCarsByIds (RTDB): Запит для carId: " + carId);
            rtdbCarsRef.child(carId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Car car = dataSnapshot.getValue(Car.class);
                    if (car != null) {
                        car.setId(dataSnapshot.getKey()); // Встановлюємо ID з ключа вузла Realtime Database
                        carList.add(car);
                        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Автомобіль знайдено: " + car.getName() + " (ID: " + car.getId() + ")");
                    } else {
                        Log.w("FirebaseHelper", "getCarsByIds (RTDB): Автомобіль не знайдено для ID: " + carId);
                    }

                    // Коли всі запити завершено (успішно чи ні), викликаємо listener
                    if (lookupsRemaining.decrementAndGet() == 0) {
                        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Всі запити RTDB завершено. Кількість знайдених авто: " + carList.size());
                        listener.onCarListLoaded(carList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseHelper", "getCarsByIds (RTDB): Помилка завантаження автомобіля для ID: " + carId, databaseError.toException());
                    if (lookupsRemaining.decrementAndGet() == 0) {
                        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Всі запити RTDB завершено (з помилками). Кількість знайдених авто: " + carList.size());
                        listener.onCarListLoaded(carList); // Повертаємо те, що вдалося завантажити
                    }
                }
            });
        }
    }

    // Інтерфейс для обробки отриманого списку автомобілів
    public interface OnCarListLoadedListener {
        void onCarListLoaded(List<Car> carList);
    }

    // Інтерфейс для обробки отриманого списку ID улюблених автомобілів
    public interface OnFavoriteCarIdsLoadedListener {
        void onFavoriteCarIdsLoaded(List<String> favoriteCarIds);
    }
}