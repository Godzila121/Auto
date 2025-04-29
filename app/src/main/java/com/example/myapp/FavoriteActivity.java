package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> favoriteCars = new ArrayList<>();  // Ініціалізуємо список, щоб уникнути NullPointerException

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Ініціалізація елементів
        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        TextView NoFavorites = findViewById(R.id.NoFavorites);

        // Обробка натискання кнопки пошуку
        buttonSearch.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, SecondActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Обробка натискання кнопки улюблених
        buttonFavorite.setOnClickListener(v -> {
            Toast.makeText(FavoriteActivity.this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
        });

        // Обробка натискання кнопки акаунту
        buttonAccount.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, AccountActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // ==== Додаємо код для виведення вподобаних машин ====

        // Отримуємо список збережених вподобаних машин
        favoriteCars = SharedPreferencesHelper.getFavorites(this);

        if (favoriteCars == null) {
            favoriteCars = new ArrayList<>();  // Якщо не знайдено, ініціалізуємо порожній список
        }
        if (!favoriteCars.isEmpty()) {
            NoFavorites.setVisibility(View.GONE);  // Приховуємо повідомлення
        } else {
            NoFavorites.setVisibility(View.VISIBLE);  // Відображаємо повідомлення
        }
        // Налаштування RecyclerView
        recyclerView = findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Створюємо адаптер із переданими машинами
        carAdapter = new CarAdapter(this, favoriteCars, favoriteCars);
        recyclerView.setAdapter(carAdapter);
    }
}
