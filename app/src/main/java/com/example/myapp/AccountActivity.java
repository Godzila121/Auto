package com.example.myapp;

import android.content.Intent;
// import android.content.SharedPreferences; // Може бути непотрібним, якщо SharedPreferencesHelper інкапсулює все
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

public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch, buttonFavorite, buttonAccountNav, iconProfile; // buttonAccount перейменовано на buttonAccountNav
    private Button buttonRegister, buttonLogout, buttonLogin;
    private EditText inputEmail, inputPassword;
    private TextView textWelcome;

    // Нові поля для списку автомобілів користувача
    private RecyclerView recyclerViewMyCars;
    private CarAdapter myCarsAdapter;
    private List<Car> myCarList;
    private List<String> myAccountFavoriteCarIds; // Для CarAdapter на цій сторінці
    private TextView textMyCarsTitle;
    private TextView textNoMyCars;

    private DatabaseReference allCarsRef; // Посилання на загальний вузол "cars" в RTDB
    private Query userCarsQuery;
    private ValueEventListener userCarsValueEventListener;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();

        // Ініціалізація існуючих View
        iconProfile = findViewById(R.id.icon_profile);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonRegister = findViewById(R.id.button_register);
        buttonLogout = findViewById(R.id.button_logout);
        buttonLogin = findViewById(R.id.button_login);
        textWelcome = findViewById(R.id.text_welcome);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccountNav = findViewById(R.id.button_account); // Використовуємо ID для кнопки навігації

        // Ініціалізація нових View та RecyclerView
        textMyCarsTitle = findViewById(R.id.text_my_cars_title);
        recyclerViewMyCars = findViewById(R.id.recycler_view_my_cars);
        textNoMyCars = findViewById(R.id.text_no_my_cars);

        myCarList = new ArrayList<>();
        myAccountFavoriteCarIds = new ArrayList<>();

        recyclerViewMyCars.setLayoutManager(new LinearLayoutManager(this));
        myCarsAdapter = new CarAdapter(this, myCarList, myAccountFavoriteCarIds);
        recyclerViewMyCars.setAdapter(myCarsAdapter);

        allCarsRef = FirebaseDatabase.getInstance().getReference("cars");

        setupAuthListener();

        // Обробники кнопок
        buttonRegister.setOnClickListener(v -> registerUser());
        buttonLogin.setOnClickListener(v -> loginUser());
        buttonLogout.setOnClickListener(v -> {
            detachUserCarsListener(); // Від'єднуємо слухача перед виходом
            mAuth.signOut(); // AuthStateListener обробить оновлення UI
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
        buttonAccountNav.setOnClickListener(v -> {
            Toast.makeText(this, "Ви вже на сторінці акаунта", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupAuthListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d("AccountActivity", "AuthState: Користувач увійшов - " + user.getEmail());
                SharedPreferencesHelper.saveLoginStatus(this, true);
                if (user.getEmail() != null) {
                    // Зберігаємо email через SharedPreferencesHelper, якщо потрібно
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
        iconProfile.setVisibility(View.GONE); // Або інша логіка

        textMyCarsTitle.setVisibility(View.GONE);
        recyclerViewMyCars.setVisibility(View.GONE);
        textNoMyCars.setVisibility(View.GONE);

        myCarList.clear();
        myAccountFavoriteCarIds.clear();
        if (myCarsAdapter != null) {
            myCarsAdapter.notifyDataSetChanged();
            // Також оновлюємо список улюблених в адаптері, оскільки він міг змінитися
            myCarsAdapter.updateFavoriteCarIds(myAccountFavoriteCarIds);
        }
        detachUserCarsListener();
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
                        // НЕ ЗБЕРІГАЙТЕ ПАРОЛЬ В SHARED PREFERENCES
                        // SharedPreferencesHelper.saveUserRegistrationInfo(this, email); // Зберігає email та статус
                        // AuthStateListener оновить UI
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
                    if (task.isSuccessful()) {
                        // AuthStateListener оновить UI
                        // SharedPreferencesHelper.saveUserEmail(this, email); // Якщо потрібно для SharedPreferencesHelper
                    } else {
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

        userCarsQuery = allCarsRef.orderByChild("userId").equalTo(currentUserId);
        Log.d("AccountActivity", "Завантаження автомобілів для userId: " + currentUserId);

        userCarsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myCarList.clear();
                if (dataSnapshot.exists()) {
                    Log.d("AccountActivity", "Знайдено автомобілів користувача: " + dataSnapshot.getChildrenCount());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Car car = snapshot.getValue(Car.class);
                        if (car != null) {
                            car.setId(snapshot.getKey());
                            myCarList.add(car);
                        }
                    }
                } else {
                    Log.d("AccountActivity", "Автомобілі користувача не знайдено.");
                }

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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountActivity", "Помилка завантаження автомобілів користувача.", databaseError.toException());
                Toast.makeText(AccountActivity.this, "Помилка завантаження ваших автомобілів", Toast.LENGTH_SHORT).show();
                if (textNoMyCars != null) textNoMyCars.setVisibility(View.VISIBLE);
                if (recyclerViewMyCars != null) recyclerViewMyCars.setVisibility(View.GONE);
            }
        };
        userCarsQuery.addValueEventListener(userCarsValueEventListener);
    }

    // У файлі AccountActivity.java

    // Змініть сигнатуру методу, якщо currentUserId більше не потрібен тут
    private void loadMyFavoriteCarIdsForAdapter() { // Видалено параметр currentUserId, якщо він був лише для цього виклику
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Отримуємо поточного користувача
        if (currentUser == null) {
            Log.w("AccountActivity", "Користувач не залогінений, неможливо завантажити ID улюблених.");
            this.myAccountFavoriteCarIds.clear();
            if (myCarsAdapter != null) {
                myCarsAdapter.updateFavoriteCarIds(this.myAccountFavoriteCarIds);
            }
            return;
        }

        // ВИПРАВЛЕНИЙ ВИКЛИК: передаємо ТІЛЬКИ слухача
        FirebaseHelper.getFavoriteCarIds((List<String> receivedFavoriteIds) -> {
            Log.d("AccountActivity", "Завантажено ID улюблених (" + (receivedFavoriteIds != null ? receivedFavoriteIds.size() : "0") + ") для адаптера профілю.");

            this.myAccountFavoriteCarIds.clear();
            if (receivedFavoriteIds != null) {
                this.myAccountFavoriteCarIds.addAll(receivedFavoriteIds);
            }

            if (myCarsAdapter != null) {
                myCarsAdapter.updateFavoriteCarIds(this.myAccountFavoriteCarIds);
            } else {
                Log.e("AccountActivity", "myCarsAdapter is null in loadMyFavoriteCarIdsForAdapter callback");
            }
        });
    }

    private void detachUserCarsListener() {
        if (userCarsQuery != null && userCarsValueEventListener != null) {
            userCarsQuery.removeEventListener(userCarsValueEventListener);
            Log.d("AccountActivity", "Слухач автомобілів користувача від'єднано.");
            userCarsValueEventListener = null;
            userCarsQuery = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthListener != null) { // Перевіряємо чи слухач ініціалізований
            mAuth.addAuthStateListener(mAuthListener);
        }
        // Якщо користувач вже увійшов, AuthStateListener викличе updateUIForLoggedInUser,
        // який завантажить автомобілі. Якщо ні, то нічого не завантажуємо.
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        detachUserCarsListener();
    }

    // Видаліть старі методи, якщо їх функціонал перенесено:
    // saveLoginState, getUserEmail, getUserPassword, logout, hideForm, showForm, showWelcome, logoutUI
}