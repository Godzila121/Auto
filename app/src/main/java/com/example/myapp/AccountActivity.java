package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch, buttonFavorite, buttonAccount, iconProfile;
    private Button buttonRegister;
    private EditText inputEmail, inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        iconProfile = findViewById(R.id.icon_profile);
        // Використовуємо вже оголошену змінну inputEmail
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonRegister = findViewById(R.id.button_register);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);


        // Реєстрація
        buttonRegister.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUser(email, password);
            Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show();
        });

        // Профіль
        iconProfile.setOnClickListener(v -> {
            if (isUserRegistered()) {
                Toast.makeText(this, "Ви вже зареєстровані!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Зареєструйтесь, будь ласка", Toast.LENGTH_SHORT).show();
            }
        });

        // Навігація
        buttonSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SecondActivity.class));  // заміни на свій клас
            overridePendingTransition(0, 0);
        });

        buttonFavorite.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteActivity.class));  // заміни на свій клас
            overridePendingTransition(0, 0);
        });

        buttonAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Ви вже на сторінці акаунта", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isUserRegistered() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isRegistered", false);
    }

    private void saveUser(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isRegistered", true);
        editor.putString("userEmail", email);
        editor.putString("userPassword", password);
        editor.apply();
    }

    private void hideForm() {
        inputEmail.setVisibility(View.GONE);
        inputPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
    }
}
