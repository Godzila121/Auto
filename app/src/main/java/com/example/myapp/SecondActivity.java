package com.example.myapp;

import android.annotation.SuppressLint;
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
    private ImageView buttonProfile;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private List<Car> favoriteCars;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        buttonProfile = findViewById(R.id.button_profile);
        recyclerView = findViewById(R.id.recyclerView);

        initializeCarList();

        Intent incomingIntent = getIntent();
        favoriteCars = (List<Car>) incomingIntent.getSerializableExtra("favorite_cars");

        if (favoriteCars == null) {
            favoriteCars = new ArrayList<>();
        }

        carAdapter = new CarAdapter(this, carList, favoriteCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carAdapter);

        buttonSearch.setOnClickListener(v -> {
            Toast.makeText(SecondActivity.this, "Ви вже на цій сторінці", Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
        });

        buttonFavorite.setOnClickListener(v -> {
            Intent favoriteIntent = new Intent(SecondActivity.this, FavoriteActivity.class);
            favoriteIntent.putExtra("favorite_cars", new ArrayList<>(favoriteCars));
            startActivity(favoriteIntent);
            overridePendingTransition(0, 0);
        });

        buttonAccount.setOnClickListener(v -> {
            Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);
            startActivity(accountIntent);
            overridePendingTransition(0, 0);
        });

        buttonProfile.setOnClickListener(v -> {
            Intent accountIntent = new Intent(SecondActivity.this, AccountActivity.class);
            startActivity(accountIntent);
            overridePendingTransition(0, 0);
        });
    }

    private void initializeCarList() {
        carList = new ArrayList<>();
        carList.add(new Car("Tesla Model S", "Electric", 2022, "USA", 79999.99));
        carList.add(new Car("BMW 5 Series", "Sedan", 2021, "Germany", 54999.99));
        carList.add(new Car("Audi A6", "Sedan", 2020, "Germany", 49999.99));
        carList.add(new Car("Honda Civic", "Compact", 2023, "Japan", 22999.99));
        carList.add(new Car("Ford Mustang", "Coupe", 2022, "USA", 55999.99));
    }
}
