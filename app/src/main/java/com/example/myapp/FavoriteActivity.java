package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
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
    private List<Car> favoriteCars = new ArrayList<>();

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Ініціалізація кнопок
        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

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
            Intent intent = new Intent(FavoriteActivity.this, AccountActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Отримання списку вподобаних машин
        Intent intent = getIntent();
        favoriteCars = (List<Car>) intent.getSerializableExtra("favorite_cars");

        if (favoriteCars == null) {
            favoriteCars = new ArrayList<>();
        }

        // Налаштування RecyclerView
        recyclerView = findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Виклик адаптера — передаємо один і той же список двічі
        carAdapter = new CarAdapter(this, favoriteCars, favoriteCars);
        recyclerView.setAdapter(carAdapter);
    }
}
