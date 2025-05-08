package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;

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
    private FloatingActionButton fabAddCar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;

    private final ActivityResultLauncher<Intent> addCarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Car newCar = (Car) result.getData().getSerializableExtra("new_car");
                    if (newCar != null) {
                        carList.add(newCar);
                        carAdapter.notifyItemInserted(carList.size() - 1);
                        SharedPreferencesHelper.saveCarList(this, carList);
                        addCarToFirebase(newCar); // Додаємо новий автомобіль у Firebase
                    }
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        SharedPreferencesHelper.getCarList(this, new SharedPreferencesHelper.OnCarListLoadedListener() {
            @Override
            public void onCarListLoaded(List<Car> cars) {
                if (cars != null) {
                    carList = cars;
                    carAdapter = new CarAdapter(SecondActivity.this, carList,favoriteCars);
                    recyclerView.setLayoutManager(new LinearLayoutManager(SecondActivity.this));
                    recyclerView.setAdapter(carAdapter);
                } else {
                    Toast.makeText(SecondActivity.this, "Не вдалося завантажити автомобілі", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Ініціалізація Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("cars");

        buttonSearch = findViewById(R.id.button_search);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonAccount = findViewById(R.id.button_account);
        buttonProfile = findViewById(R.id.button_profile);
        recyclerView = findViewById(R.id.recyclerView);
        fabAddCar = findViewById(R.id.fab_add_car);


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

        SharedPreferencesHelper.getCarList(this, new SharedPreferencesHelper.OnCarListLoadedListener() {
            @Override
            public void onCarListLoaded(List<Car> loadedCarList) {
                if (loadedCarList != null) {
                    carList.clear();
                    carList.addAll(loadedCarList);
                    carAdapter.notifyDataSetChanged();
                }
            }
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
            if (isUserRegistered()) {
                Intent intent = new Intent(SecondActivity.this, AddCarActivity.class);
                addCarLauncher.launch(intent);
            } else {
                Toast.makeText(SecondActivity.this, "Будь ласка, зареєструйтесь, щоб додавати автомобілі", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SecondActivity.this, AccountActivity.class)); // Перенаправлення на екран акаунта
            }
        });
    }

    private boolean isUserRegistered() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isRegistered", false);
    }

    // Додаємо автомобіль в Firebase
    public void addCarToFirebase(Car car) {
        if (car != null) {
            String carId = mDatabaseRef.push().getKey(); // Генерація унікального ID для автомобіля
            if (carId != null) {
                mDatabaseRef.child(carId).setValue(car)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SecondActivity.this, "Автомобіль успішно додано", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SecondActivity.this, "Помилка додавання автомобіля", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    // Завантаження автомобілів з Firebase
    public void loadCarsFromFirebase() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                carList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Car car = snapshot.getValue(Car.class); // Преобразуємо дані в об'єкт Car
                    if (car != null) {
                        carList.add(car);
                    }
                }
                carAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SecondActivity.this, "Помилка при завантаженні даних", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
