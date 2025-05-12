package com.example.myapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseHelper {

    private static final String CAR_LIST_COLLECTION_FIRESTORE = "car_list_firestore";
    private static final String CAR_LIST_NODE_RTDB = "cars";

    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_SUBCOLLECTION = "favorites";


    public static final String PURCHASE_REQUESTS_NODE_RTDB = "purchase_requests";

    private static FirebaseFirestore dbFirestore = FirebaseFirestore.getInstance();

    private static CollectionReference getUserFavoritesCollection() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return dbFirestore.collection(USERS_COLLECTION).document(currentUser.getUid()).collection(FAVORITES_SUBCOLLECTION);
        } else {
            Log.w("FirebaseHelper", "Користувач не увійшов у систему (для Firestore операцій)");
            return null;
        }
    }

    public static void saveCarToFirestore(Context context, Car car) {
        dbFirestore.collection(CAR_LIST_COLLECTION_FIRESTORE)
                .add(car)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Автомобіль додано у Firestore!", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseHelper", "Автомобіль додано у Firestore з ID: " + documentReference.getId());
                    car.setId(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка при додаванні автомобіля у Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseHelper", "Помилка додавання автомобіля у Firestore: ", e);
                });
    }

    public static void getCarListFromFirestore(final OnCarListLoadedListener listener) {
        dbFirestore.collection(CAR_LIST_COLLECTION_FIRESTORE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Car> carList = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Car car = documentSnapshot.toObject(Car.class);
                            if (car != null) {
                                car.setId(documentSnapshot.getId());
                                carList.add(car);
                            }
                        }
                    }
                    if (listener != null) listener.onCarListLoaded(carList);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Помилка отримання списку автомобілів з Firestore: ", e);
                    if (listener != null) listener.onCarListLoaded(new ArrayList<>());
                });
    }

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
                        if (listener != null) listener.onFavoriteCarIdsLoaded(favoriteCarIds);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", "Помилка при отриманні ID улюблених автомобілів (Firestore)", e);
                        if (listener != null) listener.onFavoriteCarIdsLoaded(new ArrayList<>());
                    });
        } else {
            if (listener != null) listener.onFavoriteCarIdsLoaded(new ArrayList<>());
        }
    }

    public static void getCarsByIds(List<String> carIds, final OnCarListLoadedListener listener) {
        if (carIds == null || carIds.isEmpty()) {
            Log.d("FirebaseHelper", "getCarsByIds (RTDB): Список carIds порожній.");
            if (listener != null) listener.onCarListLoaded(new ArrayList<>());
            return;
        }

        DatabaseReference rtdbCarsRef = FirebaseDatabase.getInstance().getReference(CAR_LIST_NODE_RTDB);
        List<Car> carList = new ArrayList<>();
        AtomicInteger lookupsRemaining = new AtomicInteger(carIds.size());

        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Завантаження " + carIds.size() + " автомобілів з Realtime Database.");

        for (String carId : carIds) {
            if (carId == null || carId.trim().isEmpty()) {
                Log.w("FirebaseHelper", "getCarsByIds (RTDB): Пропущено порожній або null carId.");
                if (lookupsRemaining.decrementAndGet() == 0 && listener != null) {
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
                        car.setId(dataSnapshot.getKey());
                        carList.add(car);
                        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Автомобіль знайдено: " + car.getName() + " (ID: " + car.getId() + ")");
                    } else {
                        Log.w("FirebaseHelper", "getCarsByIds (RTDB): Автомобіль не знайдено для ID: " + carId);
                    }
                    if (lookupsRemaining.decrementAndGet() == 0 && listener != null) {
                        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Всі запити RTDB завершено. Кількість знайдених авто: " + carList.size());
                        listener.onCarListLoaded(carList);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseHelper", "getCarsByIds (RTDB): Помилка завантаження автомобіля для ID: " + carId, databaseError.toException());
                    if (lookupsRemaining.decrementAndGet() == 0 && listener != null) {
                        Log.d("FirebaseHelper", "getCarsByIds (RTDB): Всі запити RTDB завершено (з помилками). Кількість знайдених авто: " + carList.size());
                        listener.onCarListLoaded(carList);
                    }
                }
            });
        }
    }

    public static void deleteCarFromRealtimeDatabase(Context context, String carId) {
        if (carId == null || carId.trim().isEmpty()) {
            Log.e("FirebaseHelper", "Неможливо видалити автомобіль: carId є null або порожній.");
            Toast.makeText(context, "Помилка: ID автомобіля недійсний.", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference carNodeRef = FirebaseDatabase.getInstance().getReference(CAR_LIST_NODE_RTDB).child(carId);
        Log.d("FirebaseHelper", "Спроба видалення автомобіля з RTDB: " + carNodeRef.toString());
        carNodeRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Автомобіль з ID " + carId + " успішно видалено з Realtime Database.");
                    Toast.makeText(context, "Автомобіль видалено.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Помилка видалення автомобіля з ID " + carId + " з Realtime Database.", e);
                    Toast.makeText(context, "Помилка при видаленні автомобіля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static void createPurchaseRequest(Context context, PurchaseRequest request) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Будь ласка, увійдіть, щоб зробити запит на купівлю", Toast.LENGTH_SHORT).show();
            Log.w("FirebaseHelper", "createPurchaseRequest: Користувач не увійшов.");
            return;
        }
        request.setBuyerId(currentUser.getUid());
        if (currentUser.getEmail() != null) {
            request.setBuyerEmail(currentUser.getEmail());
        }

        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference(PURCHASE_REQUESTS_NODE_RTDB);
        String requestId = requestsRef.push().getKey();

        if (requestId == null) {
            Toast.makeText(context, "Помилка створення запиту на купівлю (не вдалося згенерувати ID)", Toast.LENGTH_SHORT).show();
            Log.e("FirebaseHelper", "Не вдалося згенерувати requestId для purchase_request");
            return;
        }
        request.setRequestId(requestId);

        requestsRef.child(requestId).setValue(request)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Запит на купівлю для carId: " + request.getCarId() + " створено з ID: " + requestId);
                    Toast.makeText(context, "Запит на купівлю надіслано!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Помилка створення запиту на купівлю для carId: " + request.getCarId(), e);
                    Toast.makeText(context, "Не вдалося надіслати запит на купівлю.", Toast.LENGTH_SHORT).show();
                });
    }

    public static void updatePurchaseRequestStatus(Context context, String requestId, String newStatus, final OnRequestUpdatedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w("FirebaseHelper", "updatePurchaseRequestStatus: Користувач не увійшов.");
            Toast.makeText(context, "Помилка: користувача не автентифіковано.", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onFailure(new Exception("Користувача не автентифіковано"));
            return;
        }

        if (requestId == null || requestId.trim().isEmpty()) {
            Log.e("FirebaseHelper", "updatePurchaseRequestStatus: requestId є null або порожній.");
            Toast.makeText(context, "Помилка: недійсний ID запиту.", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onFailure(new Exception("Недійсний ID запиту"));
            return;
        }

        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference(PURCHASE_REQUESTS_NODE_RTDB).child(requestId);

        requestRef.child("sellerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sellerId = snapshot.getValue(String.class);
                if (currentUser.getUid().equals(sellerId)) {
                    Map<String, Object> statusUpdate = new HashMap<>();
                    statusUpdate.put("status", newStatus);

                    requestRef.updateChildren(statusUpdate)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirebaseHelper", "Статус запиту " + requestId + " оновлено на " + newStatus);
                                if (listener != null) listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseHelper", "Помилка оновлення статусу запиту " + requestId, e);
                                if (listener != null) listener.onFailure(e);
                            });
                } else {
                    Log.w("FirebaseHelper", "Спроба оновити статус запиту не власником (продавцем). CurrentUser: " +
                            currentUser.getUid() + ", SellerID у запиті: " + sellerId + ", Запит ID: " + requestId);
                    Toast.makeText(context, "Ви не можете змінити статус цього запиту.", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onFailure(new Exception("Доступ заборонено: ви не продавець"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseHelper", "Помилка перевірки sellerId для запиту " + requestId, error.toException());
                if (listener != null) listener.onFailure(error.toException());
            }
        });
    }

    public interface OnRequestUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }


    public interface OnCarListLoadedListener {
        void onCarListLoaded(List<Car> carList);
    }

    public interface OnFavoriteCarIdsLoadedListener {
        void onFavoriteCarIdsLoaded(List<String> favoriteCarIds);
    }
}