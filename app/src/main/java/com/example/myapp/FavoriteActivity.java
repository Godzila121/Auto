package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> favoriteCars = new ArrayList<>();

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private ImageView buttonProfile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Перевірка стану реєстрації
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean("isRegistered", false);

        if (!isRegistered) {
            // Перенаправлення на екран облікового запису для реєстрації
            Intent accountIntent = new Intent(FavoriteActivity.this, AccountActivity.class);
            startActivity(accountIntent);
            finish(); // Закриваємо FavoriteActivity
            return;
        }

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        TextView NoFavorites = findViewById(R.id.NoFavorites);
        buttonProfile = findViewById(R.id.button_profile);

        buttonSearch.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, SecondActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        buttonFavorite.setOnClickListener(v -> {
            Toast.makeText(FavoriteActivity.this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
        });

        buttonAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Ви вже залогінені", Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
        });

        buttonProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, AccountActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        favoriteCars = SharedPreferencesHelper.getFavorites(this);

        if (favoriteCars == null) {
            favoriteCars = new ArrayList<>();
        }

        if (!favoriteCars.isEmpty()) {
            NoFavorites.setVisibility(View.GONE);
        } else {
            NoFavorites.setVisibility(View.VISIBLE);
        }

        recyclerView = findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carAdapter = new CarAdapter(this, favoriteCars, favoriteCars);
        recyclerView.setAdapter(carAdapter);
    }
}