package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch, buttonFavorite, buttonAccount, iconProfile;
    private Button buttonRegister, buttonLogout;
    private EditText inputEmail, inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        iconProfile = findViewById(R.id.icon_profile);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonRegister = findViewById(R.id.button_register);
        buttonLogout = findViewById(R.id.button_logout);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

        if (isUserRegistered()) {
            hideForm();
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

            saveUser(email, password);
            Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show();
            hideForm();
        });

        iconProfile.setOnClickListener(v -> {
            if (isUserRegistered()) {
                String userEmail = getUserEmail();
                Toast.makeText(this, "Ви зареєстровані як: " + userEmail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Зареєструйтесь, будь ласка", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogout.setOnClickListener(v -> {
            logout();
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

    private String getUserEmail() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("userEmail", "");
    }

    private void hideForm() {
        inputEmail.setVisibility(View.GONE);
        inputPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.VISIBLE);
    }

    private void showForm() {
        inputEmail.setVisibility(View.VISIBLE);
        inputPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogout.setVisibility(View.GONE);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();


        showForm();
        Toast.makeText(this, "Ви вийшли з акаунта", Toast.LENGTH_SHORT).show();
    }
}
