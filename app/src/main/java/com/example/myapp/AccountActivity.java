package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private ImageView iconProfile;
    private Button buttonRegister;
    private EditText inputEmail;
    private EditText inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        iconProfile = findViewById(R.id.icon_profile);
        buttonRegister = findViewById(R.id.button_register);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        iconProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRegistered = checkRegistration();

                if (isRegistered) {
                    Toast.makeText(AccountActivity.this, "Ласкаво просимо до профілю!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AccountActivity.this, "Будь ласка, зареєструйтесь!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Перевірка на авторизацію (можна додати реальну перевірку)
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                if (email.equals("petadebchyk@gmail.com") && password.equals("1111")) {
                    saveRegistration();
                    Toast.makeText(AccountActivity.this, "Авторизація успішна!", Toast.LENGTH_SHORT).show();

                    // Приховуємо форму після успішної авторизації
                    hideForm();
                } else {
                    Toast.makeText(AccountActivity.this, "Невірний логін або пароль", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this, SecondActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        buttonFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this, FavoriteActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        buttonAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountActivity.this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show();
                overridePendingTransition(0, 0);
            }
        });
    }

    // Перевірка реєстрації
    private boolean checkRegistration() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isRegistered", false);
    }

    // Збереження статусу реєстрації
    private void saveRegistration() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isRegistered", true);
        editor.apply();
    }

    // Метод для приховування форми після авторизації
    private void hideForm() {
        inputEmail.setVisibility(View.GONE);
        inputPassword.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);
    }
}
