package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private ImageView buttonSearch;
    private ImageView buttonFavorite;
    private ImageView buttonAccount;
    private ImageView buttonProfile;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList; // Не ініціалізуємо тут, завантажимо з SharedPreferences
    private List<Car> favoriteCars;
    private FloatingActionButton fabAddCar;

    private final ActivityResultLauncher<Intent> addCarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Car newCar = (Car) result.getData().getSerializableExtra("new_car");
                    if (newCar != null) {
                        carList.add(newCar);
                        carAdapter.notifyItemInserted(carList.size() - 1);
                        SharedPreferencesHelper.saveCarList(this, carList); // Зберігаємо оновлений список
                    }
                }
            }
    );

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
        fabAddCar = findViewById(R.id.fab_add_car);

        // Завантажуємо збережений список автомобілів
        carList = SharedPreferencesHelper.getCarList(this);
        if (carList == null) {
            carList = new ArrayList<>();
        }

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

        fabAddCar.setOnClickListener(v -> {
            Intent intent = new Intent(SecondActivity.this, AddCarActivity.class);
            addCarLauncher.launch(intent);
        });
    }
}