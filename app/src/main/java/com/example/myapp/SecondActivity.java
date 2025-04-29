package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> favoriteCars;  // Додаємо список для улюблених машин

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        recyclerView = findViewById(R.id.recyclerView);

        initializeCarList(); // Викликаємо метод для ініціалізації списку машин

        // Отримуємо список улюблених машин переданий через Intent
        Intent incomingIntent = getIntent();  // Змінили ім'я змінної на incomingIntent
        favoriteCars = (List<Car>) incomingIntent.getSerializableExtra("favorite_cars");

        if (favoriteCars == null) {
            favoriteCars = new ArrayList<>();  // Якщо не передано, ініціалізуємо порожній список
        }

        // Створюємо адаптер, передаючи всі три параметри
        carAdapter = new CarAdapter(this, carList, favoriteCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carAdapter);

        // Обробка натискання кнопки пошуку
        buttonSearch.setOnClickListener(v -> {
            Toast.makeText(SecondActivity.this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
        });

        // Обробка натискання кнопки улюблених
        buttonFavorite.setOnClickListener(v -> {
            Intent favoriteIntent = new Intent(SecondActivity.this, FavoriteActivity.class);  // Змінили ім'я змінної на favoriteIntent
            favoriteIntent.putExtra("favorite_cars", new ArrayList<>(favoriteCars));  // Передаємо улюблені машини
            startActivity(favoriteIntent);
            overridePendingTransition(0, 0);
        });

        // Обробка натискання кнопки акаунту
        buttonAccount.setOnClickListener(v -> {
            Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);  // Змінили ім'я змінної на accountIntent
            startActivity(accountIntent);
            overridePendingTransition(0, 0);
        });
    }

    // Метод для ініціалізації списку машин
    private void initializeCarList() {
        carList = new ArrayList<>();
        carList.add(new Car("Tesla Model S", "Electric", 2022, "USA", 79999.99));
        carList.add(new Car("BMW 5 Series", "Sedan", 2021, "Germany", 54999.99));
        carList.add(new Car("Audi A6", "Sedan", 2020, "Germany", 49999.99));
        carList.add(new Car("Honda Civic", "Compact", 2023, "Japan", 22999.99));
        carList.add(new Car("Ford Mustang", "Coupe", 2022, "USA", 55999.99));
    }
}
