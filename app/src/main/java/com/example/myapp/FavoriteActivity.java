package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> favoriteCars = new ArrayList<>();
    private List<String> favoriteCarIds = new ArrayList<>(); // Для зберігання ID улюблених

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private ImageView buttonProfile;
    private TextView NoFavorites;

    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Ініціалізація Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Перевірка стану входу
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Користувач не увійшов, перенаправлення на екран облікового запису
            Intent accountIntent = new Intent(FavoriteActivity.this, AccountActivity.class);
            startActivity(accountIntent);
            finish(); // Закриваємо FavoriteActivity
            return;
        }

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        NoFavorites = findViewById(R.id.NoFavorites);
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

        buttonProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                Toast.makeText(this, "Ви залогінені як: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Будь ласка, увійдіть", Toast.LENGTH_SHORT).show();
                Intent accountIntent = new Intent(FavoriteActivity.this, AccountActivity.class);
                startActivity(accountIntent);
                finish();
            }
            overridePendingTransition(0, 0);
        });

        buttonAccount.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, AccountActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        recyclerView = findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carAdapter = new CarAdapter(this, favoriteCars, favoriteCarIds); // Передаємо favoriteCarIds
        recyclerView.setAdapter(carAdapter);

        loadFavoriteCarIdsFromFirestore();
    }

    private void loadFavoriteCarIdsFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Вже є в onCreate, але для ясності
        if (currentUser == null) {
            Log.e("FavoriteActivity", "Користувач не увійшов, неможливо завантажити ID улюблених.");
            // Можна додати оновлення UI тут, якщо потрібно
            if(NoFavorites != null) NoFavorites.setVisibility(TextView.VISIBLE);
            favoriteCars.clear();
            favoriteCarIds.clear();
            if(carAdapter != null) carAdapter.notifyDataSetChanged();
            return;
        }

        // Ваш FirebaseHelper.getFavoriteCarIds вже використовує FirebaseAuth.getInstance().getCurrentUser() всередині getUserFavoritesCollection()
        // тому передавати currentUser.getUid() явно не обов'язково, якщо FirebaseHelper не змінено
        FirebaseHelper.getFavoriteCarIds(receivedFavoriteIds -> {
            Log.d("FavoriteActivity", "FirebaseHelper.getFavoriteCarIds - отримано ID: " + receivedFavoriteIds.toString());
            this.favoriteCarIds.clear();
            if (receivedFavoriteIds != null) { // Додайте перевірку на null
                this.favoriteCarIds.addAll(receivedFavoriteIds);
            }
            Log.d("FavoriteActivity", "Список favoriteCarIds в FavoriteActivity: " + this.favoriteCarIds.toString());
            loadFavoriteCarsFromFirestore();
        });
    }

    private void loadFavoriteCarsFromFirestore() {
        if (!favoriteCarIds.isEmpty()) {
            NoFavorites.setVisibility(TextView.GONE);
            FirebaseHelper.getCarsByIds(favoriteCarIds, carList -> {
                favoriteCars.clear();
                if (carList != null && !carList.isEmpty()) {
                    favoriteCars.addAll(carList);
                } else {
                    NoFavorites.setVisibility(TextView.VISIBLE);
                }
                carAdapter.notifyDataSetChanged();
            });
        } else {
            NoFavorites.setVisibility(TextView.VISIBLE);
            favoriteCars.clear();
            carAdapter.notifyDataSetChanged();
        }
    }
}