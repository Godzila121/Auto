package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> favoriteCars;

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoriteActivity.this, SecondActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        buttonFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FavoriteActivity.this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show();
                overridePendingTransition(0, 0);
            }
        });

        buttonAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoriteActivity.this, AccountActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // ==== Додаємо код для виведення вподобаних машин ====

        // Отримуємо список переданих вподобаних машин
        Intent intent = getIntent();
        favoriteCars = (List<Car>) intent.getSerializableExtra("favorite_cars");

        // Налаштування RecyclerView
        recyclerView = findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Створюємо адаптер із переданими машинами
        carAdapter = new CarAdapter(this, favoriteCars);
        recyclerView.setAdapter(carAdapter);
    }
}
