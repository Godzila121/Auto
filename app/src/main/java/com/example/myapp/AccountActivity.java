package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch, buttonFavorite, buttonAccount, iconProfile;
    private Button buttonRegister, buttonLogout, buttonLogin; // Додано buttonLogin
    private EditText inputEmail, inputPassword;
    private TextView textWelcome;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();

        iconProfile = findViewById(R.id.icon_profile);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonRegister = findViewById(R.id.button_register);
        buttonLogout = findViewById(R.id.button_logout);
        buttonLogin = findViewById(R.id.button_login); // Ініціалізація buttonLogin
        textWelcome = findViewById(R.id.text_welcome);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

        // Перевірка стану входу при створенні Activity
        if (SharedPreferencesHelper.getLoginStatus(this)) {
            hideForm();
            showWelcome(SharedPreferencesHelper.getSavedEmail(this));
        } else {
            showForm();
        }

        buttonRegister.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SharedPreferencesHelper.saveUserCredentials(this, email, password);
                            SharedPreferencesHelper.saveLoginStatus(this, true); // Збереження стану входу
                            Toast.makeText(this, "Реєстрація у Firebase успішна!", Toast.LENGTH_SHORT).show();
                            hideForm();
                            showWelcome(email);
                        } else {
                            Toast.makeText(this, "Помилка реєстрації: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        buttonLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Вхід успішний, оновіть UI
                            hideForm();
                            showWelcome(email);
                            SharedPreferencesHelper.saveLoginStatus(this, true); // Збереження стану входу
                        } else {
                            Toast.makeText(this, "Помилка входу: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        iconProfile.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                Toast.makeText(this, "Ви увійшли як: " + userEmail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Будь ласка, увійдіть або зареєструйтесь", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            SharedPreferencesHelper.saveLoginStatus(this, false); // Оновлення стану входу
            SharedPreferencesHelper.clearUserCredentials(this);
            logoutUI();
        });

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

        // Видалено виклик autoLogin() з onCreate()
    }

    private void saveLoginState(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userEmail", email);
        editor.putString("userPassword", password);
        editor.apply();
    }

    private String getUserEmail() {
        return SharedPreferencesHelper.getSavedEmail(this);
    }

    private String getUserPassword() {
        return SharedPreferencesHelper.getSavedPassword(this);
    }

    private void hideForm() {
        inputEmail.setVisibility(View.GONE);
        inputPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE); // Приховати кнопку входу після успішного входу
        buttonLogout.setVisibility(View.VISIBLE);
        iconProfile.setVisibility(View.VISIBLE);
        textWelcome.setVisibility(View.VISIBLE);
    }

    private void showForm() {
        inputEmail.setVisibility(View.VISIBLE);
        inputPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE); // Показати кнопку входу
        buttonLogout.setVisibility(View.GONE);
        iconProfile.setVisibility(View.GONE);
        textWelcome.setVisibility(View.GONE);
    }

    private void showWelcome(String email) {
        textWelcome.setText("Ласкаво просимо, " + email);
        textWelcome.setVisibility(View.VISIBLE);
    }

    private void logout() {
        // Виклик методів SharedPreferencesHelper для оновлення стану
        SharedPreferencesHelper.saveLoginStatus(this, false);
        SharedPreferencesHelper.clearUserCredentials(this);
        logoutUI();
    }

    private void logoutUI() {
        textWelcome.setVisibility(View.GONE);
        showForm();
        Toast.makeText(this, "Ви вийшли з акаунта", Toast.LENGTH_SHORT).show();
    }
}