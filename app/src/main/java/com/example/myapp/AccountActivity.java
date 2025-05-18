package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch, buttonFavorite, buttonAccount, iconProfile;
    private Button buttonRegister, buttonLogout, buttonLogin;
    private EditText inputEmail, inputPassword;
    private TextView textWelcome;

    private RecyclerView recyclerViewMyCars;
    private CarAdapter myCarsAdapter;
    private List<Car> myCarList;
    private List<String> myAccountFavoriteCarIds;
    private TextView textMyCarsTitle;
    private TextView textNoMyCars;
    private DatabaseReference allCarsRefRTDB;
    private Query userCarsQuery;
    private ValueEventListener userCarsValueEventListener;

    private RecyclerView recyclerViewPurchaseRequests;
    private PurchaseRequestAdapter purchaseRequestAdapter;
    private List<PurchaseRequest> purchaseRequestList;
    private TextView textPurchaseRequestsTitle;
    private TextView textNoPurchaseRequests;
    private DatabaseReference allPurchaseRequestsRefRTDB;
    private Query userIncomingRequestsQuery;
    private ValueEventListener purchaseRequestsValueEventListener;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        allCarsRefRTDB = FirebaseDatabase.getInstance().getReference("cars");
        allPurchaseRequestsRefRTDB = FirebaseDatabase.getInstance().getReference(FirebaseHelper.PURCHASE_REQUESTS_NODE_RTDB);


        iconProfile = findViewById(R.id.icon_profile);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonRegister = findViewById(R.id.button_register);
        buttonLogout = findViewById(R.id.button_logout);
        buttonLogin = findViewById(R.id.button_login);
        textWelcome = findViewById(R.id.text_welcome);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

        textMyCarsTitle = findViewById(R.id.text_my_cars_title);
        recyclerViewMyCars = findViewById(R.id.recycler_view_my_cars);
        textNoMyCars = findViewById(R.id.text_no_my_cars);
        myCarList = new ArrayList<>();
        myAccountFavoriteCarIds = new ArrayList<>();
        recyclerViewMyCars.setLayoutManager(new LinearLayoutManager(this));
        // `allowDeletion = true` (можна видаляти свої авто), `allowPurchase = false` (не можна купувати свої авто)
        myCarsAdapter = new CarAdapter(this, myCarList, myAccountFavoriteCarIds, true, false);
        recyclerViewMyCars.setAdapter(myCarsAdapter);

        textPurchaseRequestsTitle = findViewById(R.id.text_purchase_requests_title);
        recyclerViewPurchaseRequests = findViewById(R.id.recycler_view_purchase_requests);
        textNoPurchaseRequests = findViewById(R.id.text_no_purchase_requests);
        purchaseRequestList = new ArrayList<>();
        recyclerViewPurchaseRequests.setLayoutManager(new LinearLayoutManager(this));
        purchaseRequestAdapter = new PurchaseRequestAdapter(this, purchaseRequestList);
        recyclerViewPurchaseRequests.setAdapter(purchaseRequestAdapter);

        setupAuthListener();

        buttonRegister.setOnClickListener(v -> registerUser());
        buttonLogin.setOnClickListener(v -> loginUser());
        buttonLogout.setOnClickListener(v -> {
            detachAllListeners();
            mAuth.signOut();
        });

        iconProfile.setOnClickListener(v -> showProfileToast());
        buttonSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SecondActivity.class));
            overridePendingTransition(0, 0);
        });
        buttonFavorite.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteActivity.class));
            overridePendingTransition(0, 0);
        });
        buttonAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Ви вже на сторінці акаунта", Toast.LENGTH_SHORT).show();
        });
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        // Створюємо NotificationChannel, але тільки для API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ID каналу. Має бути унікальним для вашого пакету.
            String channelId = getString(R.string.default_notification_channel_id); // Визначимо цей рядок у strings.xml
            // Назва каналу, яку бачить користувач у налаштуваннях додатку.
            CharSequence name = getString(R.string.default_notification_channel_name);
            // Опис каналу, який бачить користувач.
            String description = getString(R.string.default_notification_channel_description);
            // Важливість каналу. Визначає, як перериватиметься сповіщення.
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            // Реєструємо канал у системі. Після цього ви не можете змінити важливість
            // або інші налаштування поведінки сповіщень; користувач має повний контроль.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("NotificationChannel", "Канал сповіщень створено: " + channelId);
            } else {
                Log.e("NotificationChannel", "NotificationManager не знайдено.");
            }
        }
    }

    private void setupAuthListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d("AccountActivity", "AuthState: Користувач увійшов - " + user.getEmail());
                SharedPreferencesHelper.saveLoginStatus(this, true);
                if (user.getEmail() != null) {
                    SharedPreferencesHelper.saveUserEmail(this, user.getEmail());
                }
                updateUIForLoggedInUser(user);
            } else {
                Log.d("AccountActivity", "AuthState: Користувач вийшов.");
                SharedPreferencesHelper.saveLoginStatus(this, false);
                SharedPreferencesHelper.clearUserEmail(this); // Або SharedPreferencesHelper.clearUserSessionData(this);
                updateUIForLoggedOutUser();
            }
        };
    }

    private void updateUIForLoggedInUser(FirebaseUser user) {
        inputEmail.setVisibility(View.GONE);
        inputPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE);

        textWelcome.setText("Ласкаво просимо, " + user.getEmail());
        textWelcome.setVisibility(View.VISIBLE);
        buttonLogout.setVisibility(View.VISIBLE);
        iconProfile.setVisibility(View.VISIBLE);

        textMyCarsTitle.setVisibility(View.VISIBLE);
        loadUserCars(user.getUid());
        loadMyFavoriteCarIdsForAdapter();

        textPurchaseRequestsTitle.setVisibility(View.VISIBLE);
        loadIncomingPurchaseRequests(user.getUid());
    }

    private void updateUIForLoggedOutUser() {
        inputEmail.setText("");
        inputPassword.setText("");
        inputEmail.setVisibility(View.VISIBLE);
        inputPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);

        textWelcome.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.GONE);
        iconProfile.setVisibility(View.GONE);

        textMyCarsTitle.setVisibility(View.GONE);
        recyclerViewMyCars.setVisibility(View.GONE);
        textNoMyCars.setVisibility(View.GONE);
        myCarList.clear();
        myAccountFavoriteCarIds.clear();
        if (myCarsAdapter != null) {
            myCarsAdapter.notifyDataSetChanged();
            myCarsAdapter.updateFavoriteCarIds(myAccountFavoriteCarIds);
        }

        textPurchaseRequestsTitle.setVisibility(View.GONE);
        recyclerViewPurchaseRequests.setVisibility(View.GONE);
        textNoPurchaseRequests.setVisibility(View.GONE);
        purchaseRequestList.clear();
        if (purchaseRequestAdapter != null) {
            purchaseRequestAdapter.notifyDataSetChanged();
        }
        detachAllListeners();
    }

    private void registerUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show();
                        // SharedPreferencesHelper.saveUserRegistrationInfo(this, email);
                    } else {
                        Toast.makeText(this, "Помилка реєстрації: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Помилка входу: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showProfileToast() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "Ви увійшли як: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Будь ласка, увійдіть або зареєструйтесь", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserCars(String currentUserId) {
        detachUserCarsListener();
        userCarsQuery = allCarsRefRTDB.orderByChild("userId").equalTo(currentUserId);
        Log.d("AccountActivity", "Завантаження автомобілів для userId: " + currentUserId);

        userCarsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myCarList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Car car = snapshot.getValue(Car.class);
                        if (car != null) {
                            car.setId(snapshot.getKey());
                            myCarList.add(car);
                        }
                    }
                }
                updateMyCarsUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountActivity", "Помилка завантаження автомобілів користувача.", databaseError.toException());
                myCarList.clear(); // Очищаємо список у випадку помилки
                updateMyCarsUI();
            }
        };
        userCarsQuery.addValueEventListener(userCarsValueEventListener);
    }

    private void updateMyCarsUI() {
        if (myCarList.isEmpty()) {
            if (textNoMyCars != null) textNoMyCars.setVisibility(View.VISIBLE);
            if (recyclerViewMyCars != null) recyclerViewMyCars.setVisibility(View.GONE);
        } else {
            if (textNoMyCars != null) textNoMyCars.setVisibility(View.GONE);
            if (recyclerViewMyCars != null) recyclerViewMyCars.setVisibility(View.VISIBLE);
        }
        if (myCarsAdapter != null) {
            myCarsAdapter.notifyDataSetChanged();
        }
    }

    private void loadMyFavoriteCarIdsForAdapter() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            this.myAccountFavoriteCarIds.clear();
            if (myCarsAdapter != null) myCarsAdapter.updateFavoriteCarIds(this.myAccountFavoriteCarIds);
            return;
        }
        FirebaseHelper.getFavoriteCarIds((List<String> receivedFavoriteIds) -> {
            this.myAccountFavoriteCarIds.clear();
            if (receivedFavoriteIds != null) {
                this.myAccountFavoriteCarIds.addAll(receivedFavoriteIds);
            }
            if (myCarsAdapter != null) {
                myCarsAdapter.updateFavoriteCarIds(this.myAccountFavoriteCarIds);
            }
        });
    }

    private void loadIncomingPurchaseRequests(String currentUserId) {
        detachPurchaseRequestsListener();
        userIncomingRequestsQuery = allPurchaseRequestsRefRTDB.orderByChild("sellerId").equalTo(currentUserId);
        Log.d("AccountActivity", "Завантаження вхідних запитів на купівлю для sellerId: " + currentUserId);

        purchaseRequestsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                purchaseRequestList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        PurchaseRequest request = snapshot.getValue(PurchaseRequest.class);
                        if (request != null && "pending".equals(request.getStatus())) {
                            if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
                                request.setRequestId(snapshot.getKey());
                            }
                            purchaseRequestList.add(request);
                        }
                    }
                }
                updatePurchaseRequestsUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountActivity", "Помилка завантаження запитів на купівлю.", databaseError.toException());
                purchaseRequestList.clear();
                updatePurchaseRequestsUI();
            }
        };
        userIncomingRequestsQuery.addValueEventListener(purchaseRequestsValueEventListener);
    }

    private void updatePurchaseRequestsUI() {
        if (purchaseRequestList.isEmpty()) {
            if (textNoPurchaseRequests != null) textNoPurchaseRequests.setVisibility(View.VISIBLE);
            if (recyclerViewPurchaseRequests != null) recyclerViewPurchaseRequests.setVisibility(View.GONE);
        } else {
            if (textNoPurchaseRequests != null) textNoPurchaseRequests.setVisibility(View.GONE);
            if (recyclerViewPurchaseRequests != null) recyclerViewPurchaseRequests.setVisibility(View.VISIBLE);
        }
        if (purchaseRequestAdapter != null) {
            purchaseRequestAdapter.notifyDataSetChanged();
        }
    }

    private void detachUserCarsListener() {
        if (userCarsQuery != null && userCarsValueEventListener != null) {
            userCarsQuery.removeEventListener(userCarsValueEventListener);
            userCarsValueEventListener = null;
            userCarsQuery = null;
        }
    }

    private void detachPurchaseRequestsListener() {
        if (userIncomingRequestsQuery != null && purchaseRequestsValueEventListener != null) {
            userIncomingRequestsQuery.removeEventListener(purchaseRequestsValueEventListener);
            purchaseRequestsValueEventListener = null;
            userIncomingRequestsQuery = null;
        }
    }

    private void detachAllListeners() {
        detachUserCarsListener();
        detachPurchaseRequestsListener();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        detachAllListeners();
    }
}