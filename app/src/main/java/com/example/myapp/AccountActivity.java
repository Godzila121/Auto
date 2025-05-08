package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch, buttonFavorite, buttonAccount, iconProfile;
    private Button buttonRegister, buttonLogout;
    private EditText inputEmail, inputPassword;

    private FirebaseAuth mAuth;  // 🔹 Ініціалізація Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Ініціалізація Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Зв'язування елементів інтерфейсу
        iconProfile = findViewById(R.id.icon_profile);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonRegister = findViewById(R.id.button_register);
        buttonLogout = findViewById(R.id.button_logout);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

        // Перевірка, чи користувач зареєстрований
        if (isUserRegistered()) {
            hideForm();
        } else {
            showForm();
        }

        // Реєстрація користувача через Firebase
        buttonRegister.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Створення користувача через Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveUser(email, password); // Локальне збереження
                            Toast.makeText(this, "Реєстрація у Firebase успішна!", Toast.LENGTH_SHORT).show();
                            hideForm();
                        } else {
                            Toast.makeText(this, "Помилка: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Показуємо email користувача
        iconProfile.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                Toast.makeText(this, "Ви зареєстровані як: " + userEmail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Зареєструйтесь, будь ласка", Toast.LENGTH_SHORT).show();
            }
        });

        // Вихід з акаунту
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut(); // 🔹 Вихід з Firebase
            logout();
        });

        // Навігація до іншої активності
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
    }

    // Перевірка на наявність користувача в SharedPreferences
    private boolean isUserRegistered() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isRegistered", false);
    }

    // Локальне збереження користувача
    private void saveUser(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isRegistered", true);
        editor.putString("userEmail", email);
        editor.putString("userPassword", password);
        editor.apply();
    }

    // Отримання email користувача з SharedPreferences
    private String getUserEmail() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("userEmail", "");
    }

    // Сховати форму реєстрації/входу
    private void hideForm() {
        inputEmail.setVisibility(View.GONE);
        inputPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.VISIBLE);
    }

    // Показати форму реєстрації/входу
    private void showForm() {
        inputEmail.setVisibility(View.VISIBLE);
        inputPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogout.setVisibility(View.GONE);
    }

    // Логіка виходу з акаунту
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        showForm();
        Toast.makeText(this, "Ви вийшли з акаунта", Toast.LENGTH_SHORT).show();
    }
}
